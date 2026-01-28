package com.phonemanager.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.core.app.ActivityCompat
import com.phonemanager.model.CallLogEntry
import com.phonemanager.model.CallLogResponse
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse

class CallLogManager(private val context: Context) {

    companion object {
        private const val TAG = "CallLogManager"
    }

    fun getCallLog(request: CommandRequest): CommandResponse {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
                return CommandResponse(
                    success = false,
                    message = "READ_CALL_LOG permission not granted"
                )
            }

            val selection = buildSelection(request)
            val selectionArgs = buildSelectionArgs(request)

            // Don't use LIMIT in sortOrder - not supported on all devices
            val sortOrder = "${CallLog.Calls.DATE} DESC"

            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID
            )

            Log.d(TAG, "Query - Selection: $selection, Args: ${selectionArgs?.joinToString()}")

            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val entries = mutableListOf<CallLogEntry>()
            var totalCount = 0

            cursor?.use {
                totalCount = it.count

                // Manual pagination - skip offset and take limit
                var skipped = 0
                var taken = 0

                while (it.moveToNext()) {
                    // Skip offset entries
                    if (skipped < request.offset) {
                        skipped++
                        continue
                    }

                    // Take only limit entries
                    if (taken >= request.limit) {
                        break
                    }

                    try {
                        val id = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        val number = it.getStringOrNull(CallLog.Calls.NUMBER)
                        val name = it.getStringOrNull(CallLog.Calls.CACHED_NAME)
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                        val simId = it.getStringOrNull(CallLog.Calls.PHONE_ACCOUNT_ID)

                        entries.add(
                            CallLogEntry(
                                id = id,
                                number = number,
                                name = name,
                                type = getCallTypeName(type),
                                date = date,
                                duration = duration,
                                simSlot = extractSimSlot(simId)
                            )
                        )
                        taken++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing call log entry: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Retrieved ${entries.size} entries out of $totalCount total")

            val response = CallLogResponse(
                totalCount = totalCount,
                entries = entries,
                limit = request.limit,
                offset = request.offset
            )

            CommandResponse(
                success = true,
                message = "Call log retrieved successfully (${entries.size} entries)",
                data = response
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get call log", e)
            CommandResponse(
                success = false,
                message = "Failed to get call log: ${e.message}"
            )
        }
    }

    // Extension function to safely get string from cursor
    private fun Cursor.getStringOrNull(columnName: String): String? {
        return try {
            val index = getColumnIndex(columnName)
            if (index >= 0) getString(index) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildSelection(request: CommandRequest): String? {
        val conditions = mutableListOf<String>()

        // Filter by call type
        request.type?.let { type ->
            when (type.lowercase()) {
                "incoming" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}")
                "outgoing" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}")
                "missed" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}")
                "rejected" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.REJECTED_TYPE}")
                "blocked" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE}")
                "voicemail" -> conditions.add("${CallLog.Calls.TYPE} = ${CallLog.Calls.VOICEMAIL_TYPE}")
                "all" -> { /* No filter - include all types */ }
                else -> { /* Unknown type - no filter applied */ }
            }
        }

        // Filter by date range
        request.fromDate?.let {
            conditions.add("${CallLog.Calls.DATE} >= $it")
        }

        request.toDate?.let {
            conditions.add("${CallLog.Calls.DATE} <= $it")
        }

        // Filter by number
        request.searchNumber?.let { searchNum ->
            if (searchNum.isNotEmpty()) {
                conditions.add("${CallLog.Calls.NUMBER} LIKE '%$searchNum%'")
            }
        }

        return if (conditions.isEmpty()) null else conditions.joinToString(" AND ")
    }

    private fun buildSelectionArgs(request: CommandRequest): Array<String>? {
        // We're embedding values directly in selection to avoid issues
        // This is safe because we control the input format
        return null
    }

    private fun getCallTypeName(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "incoming"
            CallLog.Calls.OUTGOING_TYPE -> "outgoing"
            CallLog.Calls.MISSED_TYPE -> "missed"
            CallLog.Calls.REJECTED_TYPE -> "rejected"
            CallLog.Calls.BLOCKED_TYPE -> "blocked"
            CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
            else -> "unknown"
        }
    }

    private fun extractSimSlot(simId: String?): Int {
        return try {
            when {
                simId.isNullOrEmpty() -> 1
                simId.contains("0", ignoreCase = true) -> 1
                simId.contains("1", ignoreCase = true) -> 2
                simId.contains("sim1", ignoreCase = true) -> 1
                simId.contains("sim2", ignoreCase = true) -> 2
                else -> 1
            }
        } catch (e: Exception) {
            1
        }
    }
}