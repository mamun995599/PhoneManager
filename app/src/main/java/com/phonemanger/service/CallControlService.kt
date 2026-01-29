package com.phonemanager.service

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log

class CallControlService : InCallService() {

    companion object {
        private const val TAG = "CallControlService"

        var currentCall: Call? = null
        var callStartTime: Long? = null
        var isIncoming: Boolean = false

        private var instance: CallControlService? = null

        fun getInstance(): CallControlService? = instance

        private val callStateListeners = mutableListOf<(Call?, Int) -> Unit>()

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
            Log.d(TAG, "Call state changed: $state")

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

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "CallControlService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "CallControlService destroyed")
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "Call added: ${call.details?.handle}")

        currentCall = call
        isIncoming = call.state == Call.STATE_RINGING
        call.registerCallback(callCallback)

        callStateListeners.forEach { it(call, call.state) }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "Call removed")

        call.unregisterCallback(callCallback)
        if (currentCall == call) {
            currentCall = null
            callStartTime = null
            isIncoming = false
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        Log.d(TAG, "Audio state changed: route=${audioState?.route}, muted=${audioState?.isMuted}")
    }

    /**
     * Set mute state using InCallService's setMuted method
     */
    fun muteCall(muted: Boolean): Boolean {
        return try {
            setMuted(muted)  // Call the parent's setMuted
            Log.d(TAG, "muteCall($muted) called successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "muteCall failed", e)
            false
        }
    }

    /**
     * Set speaker on/off using InCallService's setAudioRoute method
     */
    fun routeToSpeaker(speakerOn: Boolean): Boolean {
        return try {
            val route = if (speakerOn) {
                CallAudioState.ROUTE_SPEAKER
            } else {
                CallAudioState.ROUTE_EARPIECE
            }

            setAudioRoute(route)  // Call the parent's setAudioRoute
            Log.d(TAG, "routeToSpeaker($speakerOn) - setAudioRoute($route) called")
            true
        } catch (e: Exception) {
            Log.e(TAG, "routeToSpeaker failed", e)
            false
        }
    }

    /**
     * Get current call audio state
     */
    fun getCurrentAudioState(): CallAudioState? {
        return try {
            callAudioState
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentAudioState failed", e)
            null
        }
    }

    /**
     * Check if currently muted
     */
    fun isCallMuted(): Boolean {
        return try {
            callAudioState?.isMuted ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if speaker is on
     */
    fun isCallOnSpeaker(): Boolean {
        return try {
            callAudioState?.route == CallAudioState.ROUTE_SPEAKER
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get current audio route
     */
    fun getCurrentAudioRoute(): Int {
        return try {
            callAudioState?.route ?: CallAudioState.ROUTE_EARPIECE
        } catch (e: Exception) {
            CallAudioState.ROUTE_EARPIECE
        }
    }

    /**
     * Get supported audio routes
     */
    fun getSupportedAudioRoutes(): Int {
        return try {
            callAudioState?.supportedRouteMask ?: 0
        } catch (e: Exception) {
            0
        }
    }
}