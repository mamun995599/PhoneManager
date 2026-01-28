package com.phonemanager.service

import android.telecom.Call
import android.telecom.InCallService

class CallControlService : InCallService() {

    companion object {
        var currentCall: Call? = null
        var callStartTime: Long? = null
        var isIncoming: Boolean = false

        private var callStateListeners = mutableListOf<(Call?, Int) -> Unit>()

        fun addCallStateListener(listener: (Call?, Int) -> Unit) {
            callStateListeners.add(listener)
        }

        fun removeCallStateListener(listener: (Call?, Int) -> Unit) {
            callStateListeners.remove(listener)
        }
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            super.onStateChanged(call, state)

            when (state) {
                Call.STATE_ACTIVE -> {
                    if (callStartTime == null) {
                        callStartTime = System.currentTimeMillis()
                    }
                }
                Call.STATE_DISCONNECTED -> {
                    currentCall = null
                    callStartTime = null
                    isIncoming = false
                }
            }

            callStateListeners.forEach { it(call, state) }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        isIncoming = call.state == Call.STATE_RINGING
        call.registerCallback(callCallback)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        if (currentCall == call) {
            currentCall = null
            callStartTime = null
            isIncoming = false
        }
    }
}