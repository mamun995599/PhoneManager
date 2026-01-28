package com.phonemanager.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phonemanager.MainActivity
import com.phonemanager.PhoneManagerApp
import com.phonemanager.R
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.manager.CallStateManager
import com.phonemanager.receiver.CallReceiver
import com.phonemanager.server.HttpServerHandler
import com.phonemanager.server.WebSocketServerHandler
import com.phonemanager.utils.PreferencesManager
import kotlinx.coroutines.*

class PhoneManagerService : Service() {

    companion object {
        const val TAG = "PhoneManagerService"
        const val ACTION_STOP_SERVICE = "com.phonemanager.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
    }

    private val binder = LocalBinder()
    private var webSocketServer: WebSocketServerHandler? = null
    private var httpServer: HttpServerHandler? = null

    private lateinit var callManager: CallManager
    private lateinit var callLogManager: CallLogManager
    private lateinit var callStateManager: CallStateManager
    private lateinit var prefsManager: PreferencesManager

    private var callReceiver: CallReceiver? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val logListeners = mutableListOf<(String) -> Unit>()

    inner class LocalBinder : Binder() {
        fun getService(): PhoneManagerService = this@PhoneManagerService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        callManager = CallManager(this)
        callLogManager = CallLogManager(this)
        callStateManager = CallStateManager(this)
        prefsManager = PreferencesManager(this)

        // Start listening for call state changes
        callStateManager.startListening()

        // Register broadcast receiver for call events
        registerCallReceiver()

        // Add listener for call state changes
        CallStateManager.addCallStateListener { state, number ->
            notifyLog("Call state: ${CallStateManager.getStateName(state)}, Number: $number")
            broadcastCallState(state, number)
        }
    }

    private fun registerCallReceiver() {
        try {
            callReceiver = CallReceiver()
            val filter = IntentFilter().apply {
                addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                addAction(Intent.ACTION_NEW_OUTGOING_CALL)
            }
            registerReceiver(callReceiver, filter)
            Log.d(TAG, "CallReceiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register CallReceiver", e)
        }
    }

    private fun unregisterCallReceiver() {
        try {
            callReceiver?.let {
                unregisterReceiver(it)
                Log.d(TAG, "CallReceiver unregistered")
            }
            callReceiver = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister CallReceiver", e)
        }
    }

    private fun broadcastCallState(state: Int, number: String?) {
        // Broadcast to WebSocket clients
        webSocketServer?.let { server ->
            try {
                val stateInfo = mapOf(
                    "event" to "call_state_changed",
                    "state" to CallStateManager.getStateName(state),
                    "state_code" to state,
                    "number" to number,
                    "is_active" to CallStateManager.isCallActive,
                    "is_incoming" to CallStateManager.isIncoming,
                    "timestamp" to System.currentTimeMillis()
                )
                val json = com.google.gson.Gson().toJson(stateInfo)
                server.broadcastMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast call state", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopServers()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        try {
            startForegroundWithNotification()
            acquireWakeLock()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        Log.d(TAG, "Foreground service started successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")

        callStateManager.stopListening()
        unregisterCallReceiver()
        stopServers()
        releaseWakeLock()
        serviceScope.cancel()
    }

    private fun stopServers() {
        stopWebSocketServer()
        stopHttpServer()
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, PhoneManagerService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText("Servers are running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneManager::ServiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L)
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock: ${e.message}")
        }
    }

    // WebSocket Server Control
    fun startWebSocketServer(port: Int): Boolean {
        return try {
            stopWebSocketServer()

            webSocketServer = WebSocketServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            webSocketServer?.start()
            notifyLog("WebSocket server started on port $port")
            Log.d(TAG, "WebSocket server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("WebSocket server failed: ${e.message}")
            Log.e(TAG, "WebSocket server failed", e)
            false
        }
    }

    fun stopWebSocketServer() {
        try {
            webSocketServer?.let { server ->
                server.stop(1000)
                notifyLog("WebSocket server stopped")
                Log.d(TAG, "WebSocket server stopped")
            }
            webSocketServer = null
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket stop error: ${e.message}")
            notifyLog("WebSocket stop error: ${e.message}")
        }
    }

    fun isWebSocketRunning(): Boolean = webSocketServer != null

    // HTTP Server Control
    fun startHttpServer(port: Int): Boolean {
        return try {
            stopHttpServer()

            httpServer = HttpServerHandler(port, callManager, callLogManager) { log ->
                notifyLog(log)
            }
            httpServer?.start()
            notifyLog("HTTP server started on port $port")
            Log.d(TAG, "HTTP server started on port $port")
            true
        } catch (e: Exception) {
            notifyLog("HTTP server failed: ${e.message}")
            Log.e(TAG, "HTTP server failed", e)
            false
        }
    }

    fun stopHttpServer() {
        try {
            httpServer?.let { server ->
                server.stop()
                notifyLog("HTTP server stopped")
                Log.d(TAG, "HTTP server stopped")
            }
            httpServer = null
        } catch (e: Exception) {
            Log.e(TAG, "HTTP stop error: ${e.message}")
            notifyLog("HTTP stop error: ${e.message}")
        }
    }

    fun isHttpRunning(): Boolean = httpServer != null

    // Log listeners
    fun addLogListener(listener: (String) -> Unit) {
        logListeners.add(listener)
    }

    fun removeLogListener(listener: (String) -> Unit) {
        logListeners.remove(listener)
    }

    private fun notifyLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"

        serviceScope.launch(Dispatchers.Main) {
            logListeners.forEach {
                try {
                    it(logMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Log listener error: ${e.message}")
                }
            }
        }
    }
}