package com.phonemanager.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
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
import com.phonemanager.receiver.RestartReceiver
import com.phonemanager.server.HttpServerHandler
import com.phonemanager.server.WebSocketServerHandler
import com.phonemanager.utils.PreferencesManager
import kotlinx.coroutines.*

class PhoneManagerService : Service() {

    companion object {
        const val TAG = "PhoneManagerService"
        const val ACTION_STOP_SERVICE = "com.phonemanager.STOP_SERVICE"
        const val NOTIFICATION_ID = 1
        const val RESTART_DELAY_MS = 1000L

        @Volatile
        private var isRunning = false

        fun isRunning() = isRunning
    }

    private val binder = LocalBinder()
    private var webSocketServer: WebSocketServerHandler? = null
    private var httpServer: HttpServerHandler? = null

    private var callManager: CallManager? = null
    private var callLogManager: CallLogManager? = null
    private var callStateManager: CallStateManager? = null
    private var prefsManager: PreferencesManager? = null

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
        isRunning = true

        try {
            // Initialize managers
            callManager = CallManager(this)
            callLogManager = CallLogManager(this)
            callStateManager = CallStateManager(this)
            prefsManager = PreferencesManager(this)

            // Start listening for call state changes
            callStateManager?.startListening()

            // Register broadcast receiver
            registerCallReceiver()

            // Add call state listener
            CallStateManager.addCallStateListener { state, number ->
                notifyLog("Call: ${CallStateManager.getStateName(state)} - $number")
                broadcastCallState(state, number)
            }

            Log.d(TAG, "Service onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    private fun registerCallReceiver() {
        try {
            callReceiver = CallReceiver()
            val filter = IntentFilter().apply {
                addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                addAction(Intent.ACTION_NEW_OUTGOING_CALL)
                priority = 1000
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
            }
            callReceiver = null
            Log.d(TAG, "CallReceiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering CallReceiver", e)
        }
    }

    private fun broadcastCallState(state: Int, number: String?) {
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
        Log.d(TAG, "onBind called")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind called")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind called")
        super.onRebind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stop service requested")
                prefsManager?.serviceEnabled = false
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
            Log.e(TAG, "Error starting foreground", e)

            // Try simpler notification as fallback
            try {
                val notification = createSimpleNotification()
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Log.e(TAG, "Even simple notification failed", e2)
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = createNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10-13
                startForeground(NOTIFICATION_ID, notification, 0)
            } else {
                // Android 9 and below
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "Foreground service started")
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed, trying fallback", e)
            startForeground(NOTIFICATION_ID, notification)
        }
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

        val statusText = buildString {
            append("Running")
            if (webSocketServer != null) append(" | WS:${prefsManager?.webSocketPort ?: 8030}")
            if (httpServer != null) append(" | HTTP:${prefsManager?.httpPort ?: 8040}")
        }

        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createSimpleNotification(): Notification {
        return NotificationCompat.Builder(this, PhoneManagerApp.CHANNEL_ID)
            .setContentTitle("Phone Manager")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        try {
            val notification = createNotification()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "PhoneManager::ServiceWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(60 * 60 * 1000L) // 1 hour, will be renewed
                }
                Log.d(TAG, "WakeLock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
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
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        isRunning = false

        try {
            callStateManager?.stopListening()
            unregisterCallReceiver()
            stopServers()
            releaseWakeLock()
            serviceScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }

        // Schedule restart if service was not intentionally stopped
        if (prefsManager?.serviceEnabled == true) {
            scheduleRestart()
        }

        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")

        if (prefsManager?.serviceEnabled == true) {
            scheduleRestart()
        }

        super.onTaskRemoved(rootIntent)
    }

    private fun scheduleRestart() {
        try {
            Log.d(TAG, "Scheduling service restart")

            val restartIntent = Intent(this, RestartReceiver::class.java).apply {
                action = RestartReceiver.ACTION_RESTART
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
                    pendingIntent
                )
            }

            Log.d(TAG, "Restart scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule restart", e)
        }
    }

    private fun stopServers() {
        stopWebSocketServer()
        stopHttpServer()
    }

    // ========== WebSocket Server ==========

    fun startWebSocketServer(port: Int): Boolean {
        val manager = callManager
        val logManager = callLogManager

        if (manager == null || logManager == null) {
            Log.e(TAG, "Managers not initialized")
            notifyLog("Error: Service not fully initialized")
            return false
        }

        return try {
            stopWebSocketServer()

            webSocketServer = WebSocketServerHandler(port, manager, logManager) { log ->
                notifyLog(log)
            }
            webSocketServer?.start()

            prefsManager?.webSocketPort = port
            prefsManager?.webSocketEnabled = true

            updateNotification()
            notifyLog("WebSocket server started on port $port")
            Log.d(TAG, "WebSocket server started on port $port")
            true
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket server failed", e)
            notifyLog("WebSocket failed: ${e.message}")
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
            prefsManager?.webSocketEnabled = false
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket stop error", e)
        }
    }

    fun isWebSocketRunning(): Boolean = webSocketServer != null

    // ========== HTTP Server ==========

    fun startHttpServer(port: Int): Boolean {
        val manager = callManager
        val logManager = callLogManager

        if (manager == null || logManager == null) {
            Log.e(TAG, "Managers not initialized")
            notifyLog("Error: Service not fully initialized")
            return false
        }

        return try {
            stopHttpServer()

            httpServer = HttpServerHandler(port, manager, logManager) { log ->
                notifyLog(log)
            }
            httpServer?.start()

            prefsManager?.httpPort = port
            prefsManager?.httpEnabled = true

            updateNotification()
            notifyLog("HTTP server started on port $port")
            Log.d(TAG, "HTTP server started on port $port")
            true
        } catch (e: Exception) {
            Log.e(TAG, "HTTP server failed", e)
            notifyLog("HTTP failed: ${e.message}")
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
            prefsManager?.httpEnabled = false
            updateNotification()
        } catch (e: Exception) {
            Log.e(TAG, "HTTP stop error", e)
        }
    }

    fun isHttpRunning(): Boolean = httpServer != null

    // ========== Log Listeners ==========

    fun addLogListener(listener: (String) -> Unit) {
        if (!logListeners.contains(listener)) {
            logListeners.add(listener)
        }
    }

    fun removeLogListener(listener: (String) -> Unit) {
        logListeners.remove(listener)
    }

    private fun notifyLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"

        Log.d(TAG, logMessage)

        serviceScope.launch(Dispatchers.Main) {
            logListeners.forEach { listener ->
                try {
                    listener(logMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Log listener error", e)
                }
            }
        }
    }
}