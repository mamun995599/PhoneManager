package com.phonemanager

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager

    private var phoneManagerService: PhoneManagerService? = null
    private var isServiceBound = false

    private val logBuilder = StringBuilder()

    private val requiredPermissions = mutableListOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        updatePermissionStatus()

        if (allGranted) {
            appendLog("All permissions granted")
            startAndBindService()
        } else {
            val denied = permissions.entries.filter { !it.value }.map { it.key }
            appendLog("Some permissions denied: $denied")
            showPermissionRationale()
        }
    }

    private val defaultDialerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkDefaultDialer()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val binder = service as PhoneManagerService.LocalBinder
            phoneManagerService = binder.getService()
            isServiceBound = true

            phoneManagerService?.addLogListener { log ->
                runOnUiThread {
                    appendLog(log)
                }
            }

            updateServiceStatus()
            restoreServerStates()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            phoneManagerService = null
            isServiceBound = false
            updateServiceStatus()
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

        // Check permissions first, then start service
        if (hasRequiredPermissions()) {
            startAndBindService()
        } else {
            showPermissionRationale()
        }
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

        // WebSocket toggle
        binding.switchWebSocket.setOnCheckedChangeListener { _, isChecked ->
            handleWebSocketToggle(isChecked)
        }

        // HTTP toggle
        binding.switchHttp.setOnCheckedChangeListener { _, isChecked ->
            handleHttpToggle(isChecked)
        }

        // Request permissions button
        binding.btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }

        // Set default dialer button
        binding.btnSetDefaultDialer.setOnClickListener {
            requestDefaultDialer()
        }

        // Clear log button
        binding.btnClearLog.setOnClickListener {
            logBuilder.clear()
            binding.tvLog.text = "Log cleared."
        }
    }

    private fun handleWebSocketToggle(isChecked: Boolean) {
        if (isChecked) {
            if (!isServiceBound) {
                Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show()
                binding.switchWebSocket.isChecked = false
                return
            }

            val port = binding.etWebSocketPort.text.toString().toIntOrNull() ?: 8030
            prefsManager.webSocketPort = port

            val success = phoneManagerService?.startWebSocketServer(port) ?: false
            if (!success) {
                binding.switchWebSocket.isChecked = false
                Toast.makeText(this, "Failed to start WebSocket server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopWebSocketServer()
        }
        prefsManager.webSocketEnabled = binding.switchWebSocket.isChecked
        updateWebSocketStatus()
    }

    private fun handleHttpToggle(isChecked: Boolean) {
        if (isChecked) {
            if (!isServiceBound) {
                Toast.makeText(this, "Service not ready", Toast.LENGTH_SHORT).show()
                binding.switchHttp.isChecked = false
                return
            }

            val port = binding.etHttpPort.text.toString().toIntOrNull() ?: 8040
            prefsManager.httpPort = port

            val success = phoneManagerService?.startHttpServer(port) ?: false
            if (!success) {
                binding.switchHttp.isChecked = false
                Toast.makeText(this, "Failed to start HTTP server", Toast.LENGTH_SHORT).show()
            }
        } else {
            phoneManagerService?.stopHttpServer()
        }
        prefsManager.httpEnabled = binding.switchHttp.isChecked
        updateHttpStatus()
    }

    private fun startAndBindService() {
        try {
            Log.d(TAG, "Starting and binding service")
            val serviceIntent = Intent(this, PhoneManagerService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            prefsManager.serviceEnabled = true
            appendLog("Service starting...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            appendLog("Failed to start service: ${e.message}")
            Toast.makeText(this, "Failed to start service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreServerStates() {
        // Delay to ensure service is fully ready
        binding.root.postDelayed({
            if (prefsManager.webSocketEnabled && isServiceBound) {
                binding.switchWebSocket.isChecked = true
            }
            if (prefsManager.httpEnabled && isServiceBound) {
                binding.switchHttp.isChecked = true
            }
        }, 500)
    }

    private fun updateServiceStatus() {
        if (isServiceBound) {
            binding.tvStatus.text = "Status: Running"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        } else {
            binding.tvStatus.text = "Status: Stopped"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
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
            val ip = getLocalIpAddress() ?: getWifiIpAddress()
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

    private fun getWifiIpAddress(): String? {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi IP", e)
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

        // Check default dialer
        val isDefaultDialer = isDefaultDialer()
        permissionStatus.append("${if (isDefaultDialer) "✓" else "✗"} Default Dialer (Optional)")

        binding.tvPermissions.text = permissionStatus.toString()

        binding.btnRequestPermissions.isEnabled = !allGranted
        binding.btnSetDefaultDialer.isEnabled = !isDefaultDialer
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs phone and notification permissions to:\n\n" +
                    "• Make and control calls\n" +
                    "• Access call history\n" +
                    "• Run in background\n\n" +
                    "Please grant the required permissions.")
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            startAndBindService()
        }

        // Request battery optimization exemption
        requestBatteryOptimizationExemption()
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
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
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    defaultDialerLauncher.launch(intent)
                }
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                }
                defaultDialerLauncher.launch(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting default dialer", e)
            Toast.makeText(this, "Cannot request default dialer: ${e.message}", Toast.LENGTH_LONG).show()
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

    private fun checkDefaultDialer() {
        updatePermissionStatus()
        if (isDefaultDialer()) {
            Toast.makeText(this, "Set as default dialer successfully", Toast.LENGTH_SHORT).show()
            appendLog("Set as default dialer")
        }
    }

    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message\n"

        logBuilder.append(logMessage)
        binding.tvLog.text = logBuilder.toString()

        // Auto scroll to bottom
        binding.tvLog.post {
            val scrollAmount = binding.tvLog.layout?.let {
                it.getLineTop(binding.tvLog.lineCount) - binding.tvLog.height
            } ?: 0
            if (scrollAmount > 0) {
                binding.tvLog.scrollTo(0, scrollAmount)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
            isServiceBound = false
        }
    }
}