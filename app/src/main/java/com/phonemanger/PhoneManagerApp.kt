package com.phonemanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class PhoneManagerApp : Application() {

    companion object {
        const val TAG = "PhoneManagerApp"
        const val CHANNEL_ID = "PhoneManagerServiceChannel"

        lateinit var instance: PhoneManagerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d(TAG, "Application onCreate")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Phone Manager Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Phone Manager background service"
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                    enableLights(false)
                }

                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)

                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel", e)
            }
        }
    }
}