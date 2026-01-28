package com.phonemanager.server

import android.util.Log
import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import fi.iki.elonen.NanoHTTPD

class HttpServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : NanoHTTPD(port) {

    companion object {
        private const val TAG = "HttpServerHandler"
    }

    private val gson = Gson()

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        Log.d(TAG, "Request: ${method.name} $uri")
        onLog("HTTP ${method.name} $uri")

        // Handle CORS preflight
        if (method == Method.OPTIONS) {
            return createCorsResponse()
        }

        return try {
            when {
                uri == "/" || uri == "/status" -> handleStatus()
                uri == "/api" || uri.startsWith("/api/") -> handleApiRequest(session)
                uri == "/help" -> handleHelp()
                else -> createJsonResponse(
                    Response.Status.NOT_FOUND,
                    CommandResponse(false, "Endpoint not found: $uri. Try /api or /help")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP error", e)
            onLog("HTTP error: ${e.message}")
            createJsonResponse(
                Response.Status.INTERNAL_ERROR,
                CommandResponse(false, "Server error: ${e.message}")
            )
        }
    }

    private fun createCorsResponse(): Response {
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "").apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
            addHeader("Access-Control-Max-Age", "86400")
        }
    }

    private fun handleStatus(): Response {
        return createJsonResponse(
            Response.Status.OK,
            CommandResponse(true, "Phone Manager HTTP Server is running")
        )
    }

    private fun handleHelp(): Response {
        val help = mapOf(
            "endpoints" to mapOf(
                "/" to "Server status",
                "/api" to "API endpoint (GET/POST)",
                "/help" to "This help message"
            ),
            "commands" to listOf(
                "dial - Dial number. Params: number, sim (1|2), ussd (true|false)",
                "answer - Answer incoming call",
                "reject - Reject incoming call",
                "terminate - End active call",
                "hold - Hold active call",
                "unhold - Resume held call",
                "mute - Mute microphone",
                "unmute - Unmute microphone",
                "speaker_on - Enable loudspeaker",
                "speaker_off - Disable loudspeaker",
                "get_active_call - Get active call info",
                "get_call_log - Get call history. Params: type (all|incoming|outgoing|missed), limit, offset",
                "get_audio_status - Get current audio status",
                "get_status - Get server status"
            ),
            "examples" to listOf(
                "/api?command=dial&number=1234567890&sim=1",
                "/api?command=dial&number=*123%23&sim=1&ussd=true",
                "/api?command=answer",
                "/api?command=speaker_on",
                "/api?command=get_call_log&type=missed&limit=10",
                "/api?command=get_audio_status"
            )
        )

        return createJsonResponse(
            Response.Status.OK,
            CommandResponse(true, "API Help", help)
        )
    }

    private fun handleApiRequest(session: IHTTPSession): Response {
        val request = parseRequest(session)
        Log.d(TAG, "Parsed request: $request")

        val response = processCommand(request)
        val jsonResponse = gson.toJson(response)

        Log.d(TAG, "Response: $jsonResponse")
        onLog("Response: ${response.message}")

        return createJsonResponse(Response.Status.OK, response)
    }

    private fun parseRequest(session: IHTTPSession): CommandRequest {
        val params = mutableMapOf<String, String>()

        // Parse query parameters (GET)
        session.parameters.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                params[key] = values[0]
            }
        }

        Log.d(TAG, "Query params: $params")

        // Parse body (POST)
        if (session.method == Method.POST) {
            try {
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
                if (contentLength > 0) {
                    val body = mutableMapOf<String, String>()
                    session.parseBody(body)

                    Log.d(TAG, "POST body: $body")

                    // Check if JSON body
                    val postData = body["postData"]
                    if (!postData.isNullOrEmpty()) {
                        try {
                            return gson.fromJson(postData, CommandRequest::class.java)
                        } catch (e: Exception) {
                            Log.d(TAG, "Not JSON body, using form params")
                        }
                    }

                    // Re-parse parameters after parseBody
                    session.parameters.forEach { (key, values) ->
                        if (values.isNotEmpty()) {
                            params[key] = values[0]
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing POST body", e)
            }
        }

        // Build request from parameters
        return CommandRequest(
            command = params["command"] ?: params["cmd"] ?: "",
            number = params["number"] ?: params["phone"] ?: params["num"],
            sim = params["sim"]?.toIntOrNull() ?: 1,
            isUssd = params["is_ussd"]?.toBooleanStrictOrNull()
                ?: params["ussd"]?.toBooleanStrictOrNull()
                ?: false,
            type = params["type"],
            limit = params["limit"]?.toIntOrNull() ?: 50,
            offset = params["offset"]?.toIntOrNull() ?: 0,
            fromDate = params["from_date"]?.toLongOrNull(),
            toDate = params["to_date"]?.toLongOrNull(),
            searchNumber = params["search_number"] ?: params["search"]
        )
    }

    private fun processCommand(request: CommandRequest): CommandResponse {
        if (request.command.isEmpty()) {
            return CommandResponse(
                success = false,
                message = "Command is required. Use /help for available commands."
            )
        }

        Log.d(TAG, "Processing command: ${request.command}")

        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required. Example: ?command=dial&number=1234567890&sim=1")
                } else {
                    callManager.dial(request.number, request.sim, request.isUssd)
                }
            }
            Commands.ANSWER -> callManager.answerCall()
            Commands.REJECT -> callManager.rejectCall()
            Commands.TERMINATE -> callManager.terminateCall()
            Commands.HOLD -> callManager.holdCall()
            Commands.UNHOLD -> callManager.unholdCall()
            Commands.MUTE -> callManager.mute()
            Commands.UNMUTE -> callManager.unmute()
            Commands.SPEAKER_ON -> callManager.speakerOn()
            Commands.SPEAKER_OFF -> callManager.speakerOff()
            Commands.GET_ACTIVE_CALL -> callManager.getActiveCallInfo()
            Commands.GET_CALL_LOG -> callLogManager.getCallLog(request)
            Commands.GET_AUDIO_STATUS -> callManager.getAudioStatus()
            Commands.GET_STATUS -> CommandResponse(
                success = true,
                message = "Phone Manager is running",
                data = mapOf(
                    "version" to "1.0.0",
                    "uptime" to System.currentTimeMillis()
                )
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}. Use /help for available commands."
            )
        }
    }

    private fun createJsonResponse(status: Response.Status, response: CommandResponse): Response {
        val json = gson.toJson(response)
        return newFixedLengthResponse(status, "application/json", json).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        }
    }
}