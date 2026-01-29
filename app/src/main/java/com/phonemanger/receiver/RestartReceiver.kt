package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager

class RestartReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "RestartReceiver"
        const val ACTION_RESTART = "com.phonemanager.RESTART_SERVICE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "RestartReceiver: ${intent.action}")

        if (intent.action == ACTION_RESTART) {
            val prefs = PreferencesManager(context)
            if (prefs.serviceEnabled) {
                startService(context)
            }
        }
    }

    private fun startService(context: Context) {
        try {
            val serviceIntent = Intent(context, PhoneManagerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "Service restarted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart service", e)
        }
    }
}