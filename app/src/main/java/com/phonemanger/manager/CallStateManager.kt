package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class CallStateManager(private val context: Context) {

    companion object {
        private const val TAG = "CallStateManager"

        // Current call state
        var currentCallState: Int = TelephonyManager.CALL_STATE_IDLE
        var currentCallNumber: String? = null
        var callStartTime: Long? = null
        var isIncoming: Boolean = false
        var isCallActive: Boolean = false

        // Listeners for call state changes
        private val callStateListeners = mutableListOf<(Int, String?) -> Unit>()

        fun addCallStateListener(listener: (Int, String?) -> Unit) {
            callStateListeners.add(listener)
        }

        fun removeCallStateListener(listener: (Int, String?) -> Unit) {
            callStateListeners.remove(listener)
        }

        fun notifyListeners(state: Int, number: String?) {
            callStateListeners.forEach { it(state, number) }
        }

        fun getStateName(state: Int): String {
            return when (state) {
                TelephonyManager.CALL_STATE_IDLE -> "IDLE"
                TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK (Active)"
                else -> "UNKNOWN ($state)"
            }
        }
    }

    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null

    private val handler = Handler(Looper.getMainLooper())

    fun startListening() {
        Log.d(TAG, "Starting call state listener")

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_PHONE_STATE permission not granted")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ uses TelephonyCallback
            startTelephonyCallback()
        } else {
            // Older versions use PhoneStateListener
            startPhoneStateListener()
        }
    }

    fun stopListening() {
        Log.d(TAG, "Stopping call state listener")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
            telephonyCallback = null
        } else {
            phoneStateListener?.let {
                @Suppress("DEPRECATION")
                telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            }
            phoneStateListener = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startTelephonyCallback() {
        telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                handleCallStateChange(state, null)
            }
        }

        try {
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                telephonyCallback!!
            )
            Log.d(TAG, "TelephonyCallback registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register TelephonyCallback", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun startPhoneStateListener() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                handleCallStateChange(state, phoneNumber)
            }
        }

        try {
            telephonyManager.listen(
                phoneStateListener!!,
                PhoneStateListener.LISTEN_CALL_STATE
            )
            Log.d(TAG, "PhoneStateListener registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register PhoneStateListener", e)
        }
    }

    private fun handleCallStateChange(state: Int, phoneNumber: String?) {
        Log.d(TAG, "Call state changed: ${getStateName(state)}, number: $phoneNumber")

        val previousState = currentCallState
        currentCallState = state

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Incoming call
                isIncoming = true
                isCallActive = false
                currentCallNumber = phoneNumber ?: currentCallNumber
                Log.d(TAG, "Incoming call from: $currentCallNumber")
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call is active (answered or dialing)
                isCallActive = true
                if (previousState == TelephonyManager.CALL_STATE_IDLE) {
                    // Outgoing call
                    isIncoming = false
                }
                if (callStartTime == null) {
                    callStartTime = System.currentTimeMillis()
                }
                // Try to get number if not available
                if (currentCallNumber == null) {
                    currentCallNumber = phoneNumber ?: getActiveCallNumber()
                }
                Log.d(TAG, "Call active with: $currentCallNumber, incoming: $isIncoming")
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                Log.d(TAG, "Call ended. Was with: $currentCallNumber, duration: ${getDurationSeconds()}s")
                isCallActive = false
                isIncoming = false
                currentCallNumber = null
                callStartTime = null
            }
        }

        notifyListeners(state, currentCallNumber)
    }

    private fun getActiveCallNumber(): String? {
        // Try to get from recent call log
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {

                val cursor = context.contentResolver.query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    arrayOf(android.provider.CallLog.Calls.NUMBER),
                    null,
                    null,
                    "${android.provider.CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(0)
                    } else null
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting call number from log", e)
            null
        }
    }

    fun getCurrentCallInfo(): Map<String, Any?> {
        return mapOf(
            "state" to getStateName(currentCallState),
            "state_code" to currentCallState,
            "number" to currentCallNumber,
            "is_active" to isCallActive,
            "is_incoming" to isIncoming,
            "start_time" to callStartTime,
            "duration_seconds" to getDurationSeconds()
        )
    }

    private fun getDurationSeconds(): Long {
        return callStartTime?.let {
            (System.currentTimeMillis() - it) / 1000
        } ?: 0
    }
}