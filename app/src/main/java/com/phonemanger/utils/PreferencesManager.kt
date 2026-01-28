package com.phonemanager.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "phone_manager_prefs"
        private const val KEY_WEBSOCKET_PORT = "websocket_port"
        private const val KEY_HTTP_PORT = "http_port"
        private const val KEY_WEBSOCKET_ENABLED = "websocket_enabled"
        private const val KEY_HTTP_ENABLED = "http_enabled"
        private const val KEY_SERVICE_ENABLED = "service_enabled"

        const val DEFAULT_WEBSOCKET_PORT = 8030
        const val DEFAULT_HTTP_PORT = 8040
    }

    var webSocketPort: Int
        get() = prefs.getInt(KEY_WEBSOCKET_PORT, DEFAULT_WEBSOCKET_PORT)
        set(value) = prefs.edit().putInt(KEY_WEBSOCKET_PORT, value).apply()

    var httpPort: Int
        get() = prefs.getInt(KEY_HTTP_PORT, DEFAULT_HTTP_PORT)
        set(value) = prefs.edit().putInt(KEY_HTTP_PORT, value).apply()

    var webSocketEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEBSOCKET_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_WEBSOCKET_ENABLED, value).apply()

    var httpEnabled: Boolean
        get() = prefs.getBoolean(KEY_HTTP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_HTTP_ENABLED, value).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()
}