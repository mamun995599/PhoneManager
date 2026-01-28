package com.phonemanager.model

import com.google.gson.annotations.SerializedName

// Command Request Model
data class CommandRequest(
    @SerializedName("command")
    val command: String,

    @SerializedName("number")
    val number: String? = null,

    @SerializedName("sim")
    val sim: Int = 1,  // 1 or 2

    @SerializedName("is_ussd")
    val isUssd: Boolean = false,

    // Call log query parameters
    @SerializedName("type")
    val type: String? = null,  // incoming, outgoing, missed, all

    @SerializedName("limit")
    val limit: Int = 50,

    @SerializedName("offset")
    val offset: Int = 0,

    @SerializedName("from_date")
    val fromDate: Long? = null,

    @SerializedName("to_date")
    val toDate: Long? = null,

    @SerializedName("search_number")
    val searchNumber: String? = null
)

// Command Response Model
data class CommandResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any? = null,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

// Active Call Info
data class ActiveCallInfo(
    @SerializedName("number")
    val number: String?,

    @SerializedName("state")
    val state: String,

    @SerializedName("duration")
    val duration: Long,

    @SerializedName("is_incoming")
    val isIncoming: Boolean,

    @SerializedName("start_time")
    val startTime: Long,

    @SerializedName("is_muted")
    val isMuted: Boolean,

    @SerializedName("is_on_hold")
    val isOnHold: Boolean,

    @SerializedName("is_speaker_on")
    val isSpeakerOn: Boolean
)

// Call Log Entry
data class CallLogEntry(
    @SerializedName("id")
    val id: Long,

    @SerializedName("number")
    val number: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("type")
    val type: String,

    @SerializedName("date")
    val date: Long,

    @SerializedName("duration")
    val duration: Long,

    @SerializedName("sim_slot")
    val simSlot: Int
)

// Call Log Response
data class CallLogResponse(
    @SerializedName("total_count")
    val totalCount: Int,

    @SerializedName("entries")
    val entries: List<CallLogEntry>,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("offset")
    val offset: Int
)

// Supported Commands
object Commands {
    const val DIAL = "dial"
    const val ANSWER = "answer"
    const val REJECT = "reject"
    const val TERMINATE = "terminate"
    const val HOLD = "hold"
    const val UNHOLD = "unhold"
    const val MUTE = "mute"
    const val UNMUTE = "unmute"
    const val SPEAKER_ON = "speaker_on"
    const val SPEAKER_OFF = "speaker_off"
    const val GET_ACTIVE_CALL = "get_active_call"
    const val GET_CALL_LOG = "get_call_log"
    const val GET_STATUS = "get_status"
    const val GET_AUDIO_STATUS = "get_audio_status"
}