package com.phonemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.phonemanager.manager.CallStateManager

class CallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                Log.d(TAG, "Phone state changed: $state, number: $number")

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_RINGING
                        CallStateManager.isIncoming = true
                        CallStateManager.isCallActive = false
                        number?.let { CallStateManager.currentCallNumber = it }
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_OFFHOOK
                        CallStateManager.isCallActive = true
                        if (CallStateManager.callStartTime == null) {
                            CallStateManager.callStartTime = System.currentTimeMillis()
                        }
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        CallStateManager.currentCallState = TelephonyManager.CALL_STATE_IDLE
                        CallStateManager.isCallActive = false
                        CallStateManager.isIncoming = false
                        CallStateManager.currentCallNumber = null
                        CallStateManager.callStartTime = null
                    }
                }

                CallStateManager.notifyListeners(
                    CallStateManager.currentCallState,
                    CallStateManager.currentCallNumber
                )
            }

            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                Log.d(TAG, "Outgoing call to: $number")

                CallStateManager.currentCallNumber = number
                CallStateManager.isIncoming = false
            }
        }
    }
}