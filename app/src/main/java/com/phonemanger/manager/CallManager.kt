package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telecom.Call
import android.telecom.TelecomManager
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.ActiveCallInfo
import com.phonemanager.model.CommandResponse
import com.phonemanager.service.CallControlService

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

    // Dial number or USSD
    fun dial(number: String, simSlot: Int, isUssd: Boolean): CommandResponse {
        return try {
            Log.d(TAG, "Dialing: $number on SIM$simSlot, isUSSD: $isUssd")

            val phoneAccountHandle = getPhoneAccountHandle(simSlot)

            val formattedNumber = if (isUssd) {
                Uri.encode(number)
            } else {
                number.replace(" ", "").replace("-", "")
            }

            val uri = Uri.parse("tel:$formattedNumber")

            val extras = android.os.Bundle().apply {
                phoneAccountHandle?.let {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it)
                }
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

                // Store outgoing number
                CallStateManager.currentCallNumber = number
                CallStateManager.isIncoming = false

                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    putExtras(extras)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                Log.d(TAG, "Call initiated successfully")
                CommandResponse(
                    success = true,
                    message = if (isUssd) "USSD code sent: $number on SIM$simSlot"
                    else "Dialing: $number on SIM$simSlot"
                )
            } else {
                Log.e(TAG, "CALL_PHONE permission not granted")
                CommandResponse(
                    success = false,
                    message = "CALL_PHONE permission not granted"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dial failed", e)
            CommandResponse(
                success = false,
                message = "Dial failed: ${e.message}"
            )
        }
    }

    private fun getPhoneAccountHandle(simSlot: Int): PhoneAccountHandle? {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {

                val accounts = telecomManager.callCapablePhoneAccounts
                Log.d(TAG, "Available phone accounts: ${accounts.size}")

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
                Log.d(TAG, "Call rejected")
                return CommandResponse(success = true, message = "Call rejected")
            }

            // Fallback
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
                    Log.d(TAG, "Call terminated via TelecomManager: $ended")
                    CommandResponse(success = ended, message = if (ended) "Call terminated" else "Failed to terminate call")
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
                Log.d(TAG, "Call on hold")
                CommandResponse(success = true, message = "Call on hold")
            } else {
                CommandResponse(success = false, message = "No active call to hold (requires default dialer)")
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
                Log.d(TAG, "Call resumed")
                CommandResponse(success = true, message = "Call resumed")
            } else {
                CommandResponse(success = false, message = "No held call to resume (requires default dialer)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unhold failed", e)
            CommandResponse(success = false, message = "Unhold failed: ${e.message}")
        }
    }

    // Mute microphone
    fun mute(): CommandResponse {
        return try {
            handler.post {
                audioManager.isMicrophoneMute = true
            }
            isMuted = true
            Log.d(TAG, "Microphone muted")
            CommandResponse(success = true, message = "Microphone muted")
        } catch (e: Exception) {
            Log.e(TAG, "Mute failed", e)
            CommandResponse(success = false, message = "Mute failed: ${e.message}")
        }
    }

    // Unmute microphone
    fun unmute(): CommandResponse {
        return try {
            handler.post {
                audioManager.isMicrophoneMute = false
            }
            isMuted = false
            Log.d(TAG, "Microphone unmuted")
            CommandResponse(success = true, message = "Microphone unmuted")
        } catch (e: Exception) {
            Log.e(TAG, "Unmute failed", e)
            CommandResponse(success = false, message = "Unmute failed: ${e.message}")
        }
    }

    // Enable loudspeaker
    fun speakerOn(): CommandResponse {
        return try {
            Log.d(TAG, "Enabling speaker...")

            handler.post {
                try {
                    audioManager.mode = AudioManager.MODE_IN_CALL
                    audioManager.isSpeakerphoneOn = true

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val devices = audioManager.availableCommunicationDevices
                        val speaker = devices.find {
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                        }
                        speaker?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }

                    isSpeakerOn = true
                    Log.d(TAG, "Speaker enabled: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker on failed in handler", e)
                }
            }

            Thread.sleep(100)

            CommandResponse(
                success = true,
                message = "Loudspeaker enabled"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker on failed", e)
            CommandResponse(success = false, message = "Speaker on failed: ${e.message}")
        }
    }

    // Disable loudspeaker
    fun speakerOff(): CommandResponse {
        return try {
            Log.d(TAG, "Disabling speaker...")

            handler.post {
                try {
                    audioManager.isSpeakerphoneOn = false

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val devices = audioManager.availableCommunicationDevices
                        val earpiece = devices.find {
                            it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                        }
                        earpiece?.let {
                            audioManager.setCommunicationDevice(it)
                        }
                    }

                    isSpeakerOn = false
                    Log.d(TAG, "Speaker disabled: ${audioManager.isSpeakerphoneOn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Speaker off failed in handler", e)
                }
            }

            Thread.sleep(100)

            CommandResponse(
                success = true,
                message = "Loudspeaker disabled"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Speaker off failed", e)
            CommandResponse(success = false, message = "Speaker off failed: ${e.message}")
        }
    }

    // Get audio status
    fun getAudioStatus(): CommandResponse {
        return try {
            val status = mapOf(
                "speaker_on" to audioManager.isSpeakerphoneOn,
                "microphone_muted" to audioManager.isMicrophoneMute,
                "audio_mode" to getAudioModeName(audioManager.mode),
                "volume_call" to audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
                "volume_max" to audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                "bluetooth_sco_on" to audioManager.isBluetoothScoOn,
                "wired_headset_on" to audioManager.isWiredHeadsetOn
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

    // Get active call info - IMPROVED VERSION
    fun getActiveCallInfo(): CommandResponse {
        return try {
            Log.d(TAG, "Getting active call info...")

            // Method 1: Try InCallService (works if app is default dialer)
            val inCallServiceCall = CallControlService.currentCall
            if (inCallServiceCall != null) {
                val details = inCallServiceCall.details
                val callInfo = ActiveCallInfo(
                    number = details?.handle?.schemeSpecificPart,
                    state = getCallStateName(inCallServiceCall.state),
                    duration = CallControlService.callStartTime?.let {
                        System.currentTimeMillis() - it
                    } ?: 0,
                    isIncoming = CallControlService.isIncoming,
                    startTime = CallControlService.callStartTime ?: 0,
                    isMuted = audioManager.isMicrophoneMute,
                    isOnHold = inCallServiceCall.state == Call.STATE_HOLDING,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                Log.d(TAG, "Got call info from InCallService: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via InCallService)",
                    data = callInfo
                )
            }

            // Method 2: Use CallStateManager (TelephonyManager listener)
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
                Log.d(TAG, "Got call info from CallStateManager: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via TelephonyManager)",
                    data = callInfo
                )
            }

            // Method 3: Check TelephonyManager directly
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
                Log.d(TAG, "Got call info from TelephonyManager direct: $callInfo")
                return CommandResponse(
                    success = true,
                    message = "Active call info retrieved (via TelephonyManager direct)",
                    data = callInfo
                )
            }

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
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
                    "${CallLog.Calls.TYPE} = ?",
                    arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val number = it.getString(0)
                        val date = it.getLong(2)
                        // Only return if call was in last 60 seconds
                        if (System.currentTimeMillis() - date < 60000) {
                            Log.d(TAG, "Last dialed number: $number")
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
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
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
                        val date = it.getLong(2)
                        // Only return if call was in last 60 seconds
                        if (System.currentTimeMillis() - date < 60000) {
                            Log.d(TAG, "Last received number: $number")
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
            Call.STATE_SIMULATED_RINGING -> "SIMULATED_RINGING"
            Call.STATE_AUDIO_PROCESSING -> "AUDIO_PROCESSING"
            else -> "UNKNOWN ($state)"
        }
    }
}