package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.phonemanager.service.KeepAliveService
import com.phonemanager.service.PhoneManagerService
import com.phonemanager.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "BootReceiver: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val prefs = PreferencesManager(context)
                if (prefs.serviceEnabled) {
                    startService(context)

                    // Schedule keep-alive job
                    KeepAliveService.schedule(context)
                }
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
            Log.d(TAG, "Service started after boot")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
        }
    }
}