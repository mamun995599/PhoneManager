package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.ActiveCallInfo
import com.phonemanager.model.CommandResponse
import com.phonemanager.service.CallControlService
import java.net.URLDecoder

class CallManager(private val context: Context) {

    companion object {
        private const val TAG = "CallManager"
        var isMuted = false
        var isSpeakerOn = false
    }

    private val telecomManager: TelecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val handler = Handler(Looper.getMainLooper())

    private var audioFocusRequest: AudioFocusRequest? = null

    /**
     * Dial a phone number or execute USSD code
     * Works in background without requiring an Activity
     */
    fun dial(number: String, simSlot: Int, isUssd: Boolean): CommandResponse {
        return try {
            Log.d(TAG, "Dialing: $number on SIM$simSlot, isUSSD: $isUssd")

            // Process the number
            var processedNumber = number.trim()

            // URL decode in case it was encoded
            processedNumber = try {
                URLDecoder.decode(processedNumber, "UTF-8")
            } catch (e: Exception) {
                processedNumber
            }

            // For USSD codes, ensure it ends with #
            if (isUssd && !processedNumber.endsWith("#")) {
                processedNumber = "$processedNumber#"
            }

            Log.d(TAG, "Final number to dial: $processedNumber")

            // Check permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
                return CommandResponse(
                    success = false,
                    message = "CALL_PHONE permission not granted"
                )
            }

            // Store outgoing number
            CallStateManager.currentCallNumber = processedNumber
            CallStateManager.isIncoming = false

            // Get phone account for SIM selection
            val phoneAccountHandle = getPhoneAccountHandle(simSlot)

            // Method 1: Use TelecomManager.placeCall() - Works in background
            val success = placeCallViaTelecom(processedNumber, phoneAccountHandle)

            if (success) {
                CommandResponse(
                    success = true,
                    message = if (isUssd) "USSD code sent: $processedNumber on SIM$simSlot"
                    else "Dialing: $processedNumber on SIM$simSlot"
                )
            } else {
                // Fallback: Use Intent with FLAG_ACTIVITY_NEW_TASK
                placeCallViaIntent(processedNumber, phoneAccountHandle, isUssd)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Dial failed", e)
            CommandResponse(
                success = false,
                message = "Dial failed: ${e.message}"
            )
        }
    }

    /**
     * Place call using TelecomManager - Works in background
     */
    private fun placeCallViaTelecom(number: String, phoneAccountHandle: PhoneAccountHandle?): Boolean {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

                val uri = Uri.fromParts("tel", number, null)
                val extras = Bundle().apply {
                    phoneAccountHandle?.let {
                        putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                    }
                    putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
                }

                telecomManager.placeCall(uri, extras)
                Log.d(TAG, "Call placed via TelecomManager")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "TelecomManager.placeCall failed", e)
            false
        }
    }

    /**
     * Fallback: Place call using Intent
     */
    private fun placeCallViaIntent(
        number: String,
        phoneAccountHandle: PhoneAccountHandle?,
        isUssd: Boolean
    ): CommandResponse {
        return try {
            val uri = Uri.parse("tel:${Uri.encode(number)}")

            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                addFlags(Intent.FLAG_FROM_BACKGROUND)

                phoneAccountHandle?.let {
                    putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                }
            }

            context.startActivity(intent)
            Log.d(TAG, "Call placed via Intent")

            CommandResponse(
                success = true,
                message = if (isUssd) "USSD code sent: $number" else "Dialing: $number"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Intent call failed", e)
            CommandResponse(success = false, message = "Dial failed: ${e.message}")
        }
    }

    private fun getPhoneAccountHandle(simSlot: Int): PhoneAccountHandle? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {

                val accounts = telecomManager.callCapablePhoneAccounts
                Log.d(TAG, "Available phone accounts: ${accounts.size}")

                accounts.forEachIndexed { index, account ->
                    Log.d(TAG, "Account $index: $account")
                }

                if (accounts.size > simSlot - 1 && simSlot > 0) {
                    accounts[simSlot - 1]
                } else {
                    accounts.firstOrNull()
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone account", e)
            null
        }
    }

    // Answer incoming call
    fun answerCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to answer call")

            // Try InCallService first
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                Log.d(TAG, "Call answered via InCallService")
                return CommandResponse(success = true, message = "Call answered")
            }

            // Fallback to TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    telecomManager.acceptRingingCall()
                    Log.d(TAG, "Call answered via TelecomManager")
                    CommandResponse(success = true, message = "Call answered")
                } else {
                    CommandResponse(success = false, message = "ANSWER_PHONE_CALLS permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No incoming call to answer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Answer failed", e)
            CommandResponse(success = false, message = "Answer failed: ${e.message}")
        }
    }

    // Reject incoming call
    fun rejectCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to reject call")

            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_RINGING) {
                call.reject(false, null)
                Log.d(TAG, "Call rejected via InCallService")
                return CommandResponse(success = true, message = "Call rejected")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    telecomManager.endCall()
                    CommandResponse(success = true, message = "Call rejected")
                } else {
                    CommandResponse(success = false, message = "Permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No incoming call to reject")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Reject failed", e)
            CommandResponse(success = false, message = "Reject failed: ${e.message}")
        }
    }

    // Terminate active call
    fun terminateCall(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to terminate call")

            val call = CallControlService.currentCall
            if (call != null) {
                call.disconnect()
                Log.d(TAG, "Call terminated via InCallService")
                return CommandResponse(success = true, message = "Call terminated")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                    == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    val ended = telecomManager.endCall()
                    CommandResponse(
                        success = ended,
                        message = if (ended) "Call terminated" else "Failed to terminate"
                    )
                } else {
                    CommandResponse(success = false, message = "Permission not granted")
                }
            } else {
                CommandResponse(success = false, message = "No active call to terminate")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Terminate failed", e)
            CommandResponse(success = false, message = "Terminate failed: ${e.message}")
        }
    }

    // Hold call
    fun holdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_ACTIVE) {
                call.hold()
                CommandResponse(success = true, message = "Call on hold")
            } else {
                CommandResponse(success = false, message = "No active call to hold")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hold failed", e)
            CommandResponse(success = false, message = "Hold failed: ${e.message}")
        }
    }

    // Unhold call
    fun unholdCall(): CommandResponse {
        return try {
            val call = CallControlService.currentCall
            if (call != null && call.state == Call.STATE_HOLDING) {
                call.unhold()
                CommandResponse(success = true, message = "Call resumed")
            } else {
                CommandResponse(success = false, message = "No held call to resume")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unhold failed", e)
            CommandResponse(success = false, message = "Unhold failed: ${e.message}")
        }
    }

    // Mute microphone
    fun mute(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to mute")

            // Method 1: Use InCallService
            val service = CallControlService.getInstance()
            if (service != null) {
                val success = service.muteCall(true)  // CHANGED: setMuted -> muteCall
                if (success) {
                    isMuted = true
                    Log.d(TAG, "Muted via InCallService")
                    return CommandResponse(success = true, message = "Microphone muted")
                }
            }

            // Method 2: Use AudioManager
            handler.post {
                audioManager.isMicrophoneMute = true
            }
            isMuted = true
            Log.d(TAG, "Muted via AudioManager")
            CommandResponse(success = true, message = "Microphone muted")

        } catch (e: Exception) {
            Log.e(TAG, "Mute failed", e)
            CommandResponse(success = false, message = "Mute failed: ${e.message}")
        }
    }

    // Unmute microphone
    fun unmute(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to unmute")

            // Method 1: Use InCallService
            val service = CallControlService.getInstance()
            if (service != null) {
                val success = service.muteCall(false)  // CHANGED: setMuted -> muteCall
                if (success) {
                    isMuted = false
                    Log.d(TAG, "Unmuted via InCallService")
                    return CommandResponse(success = true, message = "Microphone unmuted")
                }
            }

            // Method 2: Use AudioManager
            handler.post {
                audioManager.isMicrophoneMute = false
            }
            isMuted = false
            Log.d(TAG, "Unmuted via AudioManager")
            CommandResponse(success = true, message = "Microphone unmuted")

        } catch (e: Exception) {
            Log.e(TAG, "Unmute failed", e)
            CommandResponse(success = false, message = "Unmute failed: ${e.message}")
        }
    }

    // Enable loudspeaker
    fun speakerOn(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to enable speaker")

            // Method 1: Use InCallService (Best method for calls)
            val service = CallControlService.getInstance()
            if (service != null) {
                val success = service.routeToSpeaker(true)  // CHANGED: setSpeakerOn -> routeToSpeaker
                if (success) {
                    isSpeakerOn = true
                    Log.d(TAG, "Speaker enabled via InCallService")
                    return CommandResponse(success = true, message = "Loudspeaker enabled")
                }
            }

            // Method 2: Use AudioManager with proper sequence
            handler.post {
                try {
                    // Request audio focus first
                    requestAudioFocus()

                    // Set mode
                    audioManager.mode = AudioManager.MODE_IN_CALL

                    // Enable speaker
                    audioManager.isSpeakerphoneOn = true

                    // For Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            val devices = audioManager.availableCommunicationDevices
                            val speaker = devices.find {
                                it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                            }
                            speaker?.let {
                                val result = audioManager.setCommunicationDevice(it)
                                Log.d(TAG, "setCommunicationDevice result: $result")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "setCommunicationDevice failed", e)
                        }
                    }

                    isSpeakerOn = true
                    Log.d(TAG, "Speaker enabled via AudioManager: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker enable failed in handler", e)
                }
            }

            // Wait for handler to complete
            Thread.sleep(200)

            CommandResponse(
                success = true,
                message = "Loudspeaker enabled (Speaker: ${audioManager.isSpeakerphoneOn})"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Speaker on failed", e)
            CommandResponse(success = false, message = "Speaker on failed: ${e.message}")
        }
    }

    // Disable loudspeaker
    fun speakerOff(): CommandResponse {
        return try {
            Log.d(TAG, "Attempting to disable speaker")

            // Method 1: Use InCallService
            val service = CallControlService.getInstance()
            if (service != null) {
                val success = service.routeToSpeaker(false)  // CHANGED: setSpeakerOn -> routeToSpeaker
                if (success) {
                    isSpeakerOn = false
                    Log.d(TAG, "Speaker disabled via InCallService")
                    return CommandResponse(success = true, message = "Loudspeaker disabled")
                }
            }

            // Method 2: Use AudioManager
            handler.post {
                try {
                    audioManager.isSpeakerphoneOn = false

                    // For Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            val devices = audioManager.availableCommunicationDevices
                            val earpiece = devices.find {
                                it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                            }
                            earpiece?.let {
                                audioManager.setCommunicationDevice(it)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "setCommunicationDevice failed", e)
                        }
                    }

                    isSpeakerOn = false
                    Log.d(TAG, "Speaker disabled via AudioManager: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker disable failed in handler", e)
                }
            }

            Thread.sleep(200)

            CommandResponse(
                success = true,
                message = "Loudspeaker disabled (Speaker: ${audioManager.isSpeakerphoneOn})"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Speaker off failed", e)
            CommandResponse(success = false, message = "Speaker off failed: ${e.message}")
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    // Get audio status
    fun getAudioStatus(): CommandResponse {
        return try {
            val callAudioState = CallControlService.getInstance()?.getCurrentAudioState()

            val status = mapOf(
                "speaker_on" to audioManager.isSpeakerphoneOn,
                "microphone_muted" to audioManager.isMicrophoneMute,
                "audio_mode" to getAudioModeName(audioManager.mode),
                "volume_call" to audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                "volume_max" to audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                "bluetooth_sco_on" to audioManager.isBluetoothScoOn,
                "wired_headset_on" to audioManager.isWiredHeadsetOn,
                "call_audio_route" to (callAudioState?.route?.let { getAudioRouteName(it) } ?: "N/A"),
                "call_is_muted" to (callAudioState?.isMuted ?: false)
            )

            CommandResponse(
                success = true,
                message = "Audio status retrieved",
                data = status
            )
        } catch (e: Exception) {
            CommandResponse(success = false, message = "Failed to get audio status: ${e.message}")
        }
    }

    private fun getAudioModeName(mode: Int): String {
        return when (mode) {
            AudioManager.MODE_NORMAL -> "NORMAL"
            AudioManager.MODE_RINGTONE -> "RINGTONE"
            AudioManager.MODE_IN_CALL -> "IN_CALL"
            AudioManager.MODE_IN_COMMUNICATION -> "IN_COMMUNICATION"
            AudioManager.MODE_CALL_SCREENING -> "CALL_SCREENING"
            else -> "UNKNOWN ($mode)"
        }
    }

    private fun getAudioRouteName(route: Int): String {
        return when (route) {
            CallAudioState.ROUTE_EARPIECE -> "EARPIECE"
            CallAudioState.ROUTE_SPEAKER -> "SPEAKER"
            CallAudioState.ROUTE_WIRED_HEADSET -> "WIRED_HEADSET"
            CallAudioState.ROUTE_BLUETOOTH -> "BLUETOOTH"
            else -> "UNKNOWN ($route)"
        }
    }

    // Get active call info - COMPLETE VERSION
    fun getActiveCallInfo(): CommandResponse {
        return try {
            Log.d(TAG, "Getting active call info...")

            // Method 1: Try InCallService
            val inCallServiceCall = CallControlService.currentCall
            if (inCallServiceCall != null) {
                val details = inCallServiceCall.details
                val callAudioState = CallControlService.getInstance()?.getCurrentAudioState()

                val callInfo = ActiveCallInfo(
                    number = details?.handle?.schemeSpecificPart,
                    state = getCallStateName(inCallServiceCall.state),
                    duration = CallControlService.callStartTime?.let {
                        System.currentTimeMillis() - it
                    } ?: 0,
                    isIncoming = CallControlService.isIncoming,
                    startTime = CallControlService.callStartTime ?: 0,
                    isMuted = callAudioState?.isMuted ?: audioManager.isMicrophoneMute,
                    isOnHold = inCallServiceCall.state == Call.STATE_HOLDING,
                    isSpeakerOn = callAudioState?.route == CallAudioState.ROUTE_SPEAKER
                            || audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from InCallService")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via InCallService)",
                    data = callInfo
                )
            }

            // Method 2: Use CallStateManager
            if (CallStateManager.isCallActive ||
                CallStateManager.currentCallState != TelephonyManager.CALL_STATE_IDLE) {

                val callInfo = ActiveCallInfo(
                    number = CallStateManager.currentCallNumber ?: getLastDialedNumber(),
                    state = CallStateManager.getStateName(CallStateManager.currentCallState),
                    duration = CallStateManager.callStartTime?.let {
                        System.currentTimeMillis() - it
                    } ?: 0,
                    isIncoming = CallStateManager.isIncoming,
                    startTime = CallStateManager.callStartTime ?: 0,
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = false,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from CallStateManager")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via TelephonyManager)",
                    data = callInfo
                )
            }

            // Method 3: Check TelephonyManager directly
            @Suppress("DEPRECATION")
            val callState = telephonyManager.callState
            if (callState != TelephonyManager.CALL_STATE_IDLE) {
                val lastNumber = getLastDialedNumber() ?: getLastReceivedNumber()
                val callInfo = ActiveCallInfo(
                    number = lastNumber,
                    state = when (callState) {
                        TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                        TelephonyManager.CALL_STATE_OFFHOOK -> "ACTIVE"
                        else -> "UNKNOWN"
                    },
                    duration = 0,
                    isIncoming = callState == TelephonyManager.CALL_STATE_RINGING,
                    startTime = System.currentTimeMillis(),
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = false,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from TelephonyManager direct")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved",
                    data = callInfo
                )
            }

            // No active call
            Log.d(TAG, "No active call found")
            CommandResponse(
                success = true,
                message = "No active call",
                data = null
            )

        } catch (e: Exception) {
            Log.e(TAG, "Get active call failed", e)
            CommandResponse(success = false, message = "Get active call failed: ${e.message}")
        }
    }

    private fun getLastDialedNumber(): String? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {

                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
                    "${CallLog.Calls.TYPE} = ?",
                    arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val number = it.getString(0)
                        val date = it.getLong(1)
                        if (System.currentTimeMillis() - date < 60000) {
                            return number
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last dialed number", e)
            null
        }
    }

    private fun getLastReceivedNumber(): String? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {

                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
                    "${CallLog.Calls.TYPE} IN (?, ?)",
                    arrayOf(
                        CallLog.Calls.INCOMING_TYPE.toString(),
                        CallLog.Calls.MISSED_TYPE.toString()
                    ),
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val number = it.getString(0)
                        val date = it.getLong(1)
                        if (System.currentTimeMillis() - date < 60000) {
                            return number
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last received number", e)
            null
        }
    }

    private fun getCallStateName(state: Int): String {
        return when (state) {
            Call.STATE_NEW -> "NEW"
            Call.STATE_DIALING -> "DIALING"
            Call.STATE_RINGING -> "RINGING"
            Call.STATE_HOLDING -> "HOLDING"
            Call.STATE_ACTIVE -> "ACTIVE"
            Call.STATE_DISCONNECTED -> "DISCONNECTED"
            Call.STATE_CONNECTING -> "CONNECTING"
            Call.STATE_DISCONNECTING -> "DISCONNECTING"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
            else -> "UNKNOWN ($state)"
        }
    }
}