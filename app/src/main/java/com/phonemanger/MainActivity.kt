package com.phonemanager

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.phonemanager.databinding.ActivityMainBinding
import com.phonemanager.service.KeepAliveService
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_DEFAULT_DIALER = 1001
        private const val SERVICE_BIND_RETRY_DELAY = 2000L
        private const val MAX_BIND_RETRIES = 3
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager

    private var phoneManagerService: PhoneManagerService? = null
    private var isServiceBound = false
    private var bindRetryCount = 0

    private val handler = Handler(Looper.getMainLooper())
    private val logBuilder = StringBuilder()

    private val requiredPermissions = mutableListOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_CONTACTS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermissionStatus()

        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            appendLog("All permissions granted")
        } else {
            val denied = permissions.entries.filter { !it.value }.map {
                it.key.substringAfterLast(".")
            }
            appendLog("Denied: ${denied.joinToString(", ")}")
        }

        // Try to start service regardless of permissions
        startAndBindService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            try {
                val binder = service as PhoneManagerService.LocalBinder
                phoneManagerService = binder.getService()
                isServiceBound = true
                bindRetryCount = 0

                phoneManagerService?.addLogListener { log ->
                    runOnUiThread { appendLog(log) }
                }

                runOnUiThread {
                    updateServiceStatus()
                    appendLog("Service connected successfully")

                    // Restore server states after a short delay
                    handler.postDelayed({
                        restoreServerStates()
                    }, 500)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onServiceConnected", e)
                runOnUiThread {
                    appendLog("Service connection error: ${e.message}")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            phoneManagerService = null
            isServiceBound = false

            runOnUiThread {
                updateServiceStatus()
                appendLog("Service disconnected")

                // Try to rebind
                if (prefsManager.serviceEnabled) {
                    handler.postDelayed({
                        startAndBindService()
                    }, SERVICE_BIND_RETRY_DELAY)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        setupUI()
        updatePermissionStatus()
        updateIpAddress()

        // Always try to start service
        startAndBindService()
    }

    private fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun setupUI() {
        binding.etWebSocketPort.setText(prefsManager.webSocketPort.toString())
        binding.etHttpPort.setText(prefsManager.httpPort.toString())
        binding.tvLog.movementMethod = ScrollingMovementMethod()

        // Disable switches initially until service is ready
        binding.switchWebSocket.isEnabled = false
        binding.switchHttp.isEnabled = false

        binding.switchWebSocket.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchWebSocket.isEnabled) {
                handleWebSocketToggle(isChecked)
            }
        }

        binding.switchHttp.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchHttp.isEnabled) {
                handleHttpToggle(isChecked)
            }
        }

        binding.btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }

        binding.btnSetDefaultDialer.setOnClickListener {
            requestDefaultDialer()
        }

        binding.btnClearLog.setOnClickListener {
            logBuilder.clear()
            binding.tvLog.text = "Log cleared."
        }
    }

    private fun handleWebSocketToggle(isChecked: Boolean) {
        if (!isServiceBound || phoneManagerService == null) {
            Toast.makeText(this, "Service not ready. Retrying...", Toast.LENGTH_SHORT).show()
            binding.switchWebSocket.isChecked = false
            startAndBindService()
            return
        }

        if (isChecked) {
            val port = binding.etWebSocketPort.text.toString().toIntOrNull() ?: 8030
            appendLog("Starting WebSocket server on port $port...")

            val success = phoneManagerService?.startWebSocketServer(port) ?: false
            if (!success) {
                binding.switchWebSocket.isChecked = false
                Toast.makeText(this, "Failed to start WebSocket server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopWebSocketServer()
        }
        updateWebSocketStatus()
    }

    private fun handleHttpToggle(isChecked: Boolean) {
        if (!isServiceBound || phoneManagerService == null) {
            Toast.makeText(this, "Service not ready. Retrying...", Toast.LENGTH_SHORT).show()
            binding.switchHttp.isChecked = false
            startAndBindService()
            return
        }

        if (isChecked) {
            val port = binding.etHttpPort.text.toString().toIntOrNull() ?: 8040
            appendLog("Starting HTTP server on port $port...")

            val success = phoneManagerService?.startHttpServer(port) ?: false
            if (!success) {
                binding.switchHttp.isChecked = false
                Toast.makeText(this, "Failed to start HTTP server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopHttpServer()
        }
        updateHttpStatus()
    }

    private fun startAndBindService() {
        if (isServiceBound && phoneManagerService != null) {
            Log.d(TAG, "Service already bound")
            updateServiceStatus()
            return
        }

        if (bindRetryCount >= MAX_BIND_RETRIES) {
            appendLog("Failed to bind service after $MAX_BIND_RETRIES attempts")
            Toast.makeText(this, "Service failed to start. Please restart the app.", Toast.LENGTH_LONG).show()
            return
        }

        bindRetryCount++
        appendLog("Starting service (attempt $bindRetryCount)...")

        try {
            val serviceIntent = Intent(this, PhoneManagerService::class.java)

            // Start the service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            // Bind to the service
            val bound = bindService(
                serviceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
            )

            Log.d(TAG, "bindService returned: $bound")

            if (!bound) {
                appendLog("Failed to bind service")

                // Retry after delay
                handler.postDelayed({
                    startAndBindService()
                }, SERVICE_BIND_RETRY_DELAY)
            } else {
                prefsManager.serviceEnabled = true

                // Schedule keep-alive
                try {
                    KeepAliveService.schedule(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to schedule KeepAliveService", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            appendLog("Error starting service: ${e.message}")

            // Retry after delay
            handler.postDelayed({
                startAndBindService()
            }, SERVICE_BIND_RETRY_DELAY)
        }
    }

    private fun restoreServerStates() {
        if (!isServiceBound || phoneManagerService == null) {
            Log.d(TAG, "Cannot restore server states - service not bound")
            return
        }

        // Enable the switches now that service is ready
        binding.switchWebSocket.isEnabled = true
        binding.switchHttp.isEnabled = true

        // Restore WebSocket server
        if (prefsManager.webSocketEnabled) {
            val port = prefsManager.webSocketPort
            appendLog("Restoring WebSocket server on port $port...")

            val success = phoneManagerService?.startWebSocketServer(port) ?: false
            binding.switchWebSocket.isChecked = success

            if (success) {
                appendLog("WebSocket server restored")
            }
        }

        // Restore HTTP server
        if (prefsManager.httpEnabled) {
            val port = prefsManager.httpPort
            appendLog("Restoring HTTP server on port $port...")

            val success = phoneManagerService?.startHttpServer(port) ?: false
            binding.switchHttp.isChecked = success

            if (success) {
                appendLog("HTTP server restored")
            }
        }

        updateWebSocketStatus()
        updateHttpStatus()
    }

    private fun updateServiceStatus() {
        val isReady = isServiceBound && phoneManagerService != null

        binding.switchWebSocket.isEnabled = isReady
        binding.switchHttp.isEnabled = isReady

        if (isReady) {
            binding.tvStatus.text = "Status: Running"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        } else {
            binding.tvStatus.text = "Status: Not Ready"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        }
    }

    private fun updateWebSocketStatus() {
        val isRunning = phoneManagerService?.isWebSocketRunning() ?: false
        val port = binding.etWebSocketPort.text.toString()
        binding.tvWebSocketStatus.text = if (isRunning) "Status: ON (Port: $port)" else "Status: OFF"
        binding.tvWebSocketStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }

    private fun updateHttpStatus() {
        val isRunning = phoneManagerService?.isHttpRunning() ?: false
        val port = binding.etHttpPort.text.toString()
        binding.tvHttpStatus.text = if (isRunning) "Status: ON (Port: $port)" else "Status: OFF"
        binding.tvHttpStatus.setTextColor(
            if (isRunning) ContextCompat.getColor(this, android.R.color.holo_green_light)
            else ContextCompat.getColor(this, android.R.color.holo_red_light)
        )
    }

    private fun updateIpAddress() {
        try {
            val ip = getLocalIpAddress()
            binding.tvIpAddress.text = "IP: ${ip ?: "Not connected"}"
        } catch (e: Exception) {
            binding.tvIpAddress.text = "IP: Unable to get IP"
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP", e)
        }
        return null
    }

    private fun updatePermissionStatus() {
        val permissionStatus = StringBuilder()
        var allGranted = true

        for (permission in requiredPermissions) {
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            val permName = permission.substringAfterLast(".")
            val status = if (granted) "✓" else "✗"
            permissionStatus.append("$status $permName\n")
            if (!granted) allGranted = false
        }

        val isDefaultDialer = isDefaultDialer()
        permissionStatus.append("${if (isDefaultDialer) "✓" else "○"} Default Dialer (Optional)")

        binding.tvPermissions.text = permissionStatus.toString()
        binding.btnRequestPermissions.isEnabled = !allGranted
        binding.btnSetDefaultDialer.isEnabled = !isDefaultDialer
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            appendLog("All permissions already granted")
            startAndBindService()
        }

        // Also request battery optimization exemption
        requestBatteryOptimizationExemption()
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    AlertDialog.Builder(this)
                        .setTitle("Battery Optimization")
                        .setMessage("For reliable background operation, please disable battery optimization for this app.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        }
                        .setNegativeButton("Later", null)
                        .show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Please disable battery optimization manually", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestDefaultDialer() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                        startActivityForResult(intent, REQUEST_DEFAULT_DIALER)
                    } else {
                        Toast.makeText(this, "Already set as default dialer", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                startActivityForResult(intent, REQUEST_DEFAULT_DIALER)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting default dialer", e)
            Toast.makeText(this, "Cannot request default dialer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_DEFAULT_DIALER) {
            updatePermissionStatus()
            if (isDefaultDialer()) {
                Toast.makeText(this, "Set as default dialer!", Toast.LENGTH_SHORT).show()
                appendLog("Set as default dialer")
            }
        }
    }

    private fun isDefaultDialer(): Boolean {
        return try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            packageName == telecomManager.defaultDialerPackage
        } catch (e: Exception) {
            false
        }
    }

    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message\n"

        logBuilder.append(logMessage)

        // Keep log size manageable
        if (logBuilder.length > 10000) {
            logBuilder.delete(0, logBuilder.length - 8000)
        }

        binding.tvLog.text = logBuilder.toString()

        // Auto scroll to bottom
        binding.tvLog.post {
            val layout = binding.tvLog.layout
            if (layout != null) {
                val scrollAmount = layout.getLineTop(binding.tvLog.lineCount) - binding.tvLog.height
                if (scrollAmount > 0) {
                    binding.tvLog.scrollTo(0, scrollAmount)
                } else {
                    binding.tvLog.scrollTo(0, 0)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateIpAddress()
        updateServiceStatus()
        updateWebSocketStatus()
        updateHttpStatus()

        // Try to rebind if not bound
        if (!isServiceBound) {
            bindRetryCount = 0
            startAndBindService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove log listener
        phoneManagerService?.removeLogListener { }

        // Unbind service (but don't stop it)
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
            isServiceBound = false
        }

        handler.removeCallbacksAndMessages(null)
    }
}