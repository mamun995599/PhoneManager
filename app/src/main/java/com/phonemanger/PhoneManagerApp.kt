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
        createNotificationChannel()
        Log.d(TAG, "Application created")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Phone Manager Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Phone Manager background service notification"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }
}