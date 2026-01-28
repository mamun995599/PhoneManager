package com.phonemanager.server

import android.util.Log
import com.google.gson.Gson
import com.phonemanager.manager.CallLogManager
import com.phonemanager.manager.CallManager
import com.phonemanager.model.CommandRequest
import com.phonemanager.model.CommandResponse
import com.phonemanager.model.Commands
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class WebSocketServerHandler(
    port: Int,
    private val callManager: CallManager,
    private val callLogManager: CallLogManager,
    private val onLog: (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    companion object {
        private const val TAG = "WebSocketServerHandler"
    }

    private val gson = Gson()

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        Log.d(TAG, "Client connected: $clientAddress")
        onLog("WebSocket client connected: $clientAddress")

        // Send welcome message
        val welcome = CommandResponse(
            success = true,
            message = "Connected to Phone Manager WebSocket Server"
        )
        conn?.send(gson.toJson(welcome))
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        val clientAddress = conn?.remoteSocketAddress?.toString() ?: "unknown"
        Log.d(TAG, "Client disconnected: $clientAddress, code: $code, reason: $reason")
        onLog("WebSocket client disconnected: $clientAddress")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let { msg ->
            Log.d(TAG, "Received: $msg")
            onLog("WS received: $msg")

            try {
                val request = gson.fromJson(msg, CommandRequest::class.java)
                val response = processCommand(request)
                val jsonResponse = gson.toJson(response)

                Log.d(TAG, "Sending: $jsonResponse")
                conn?.send(jsonResponse)
                onLog("WS response: ${response.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                val errorResponse = CommandResponse(
                    success = false,
                    message = "Invalid request format: ${e.message}"
                )
                conn?.send(gson.toJson(errorResponse))
            }
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.e(TAG, "WebSocket error", ex)
        onLog("WebSocket error: ${ex?.message}")
    }

    override fun onStart() {
        Log.d(TAG, "WebSocket server started on port: $port")
        onLog("WebSocket server started on port: $port")
        connectionLostTimeout = 100
    }

    private fun processCommand(request: CommandRequest): CommandResponse {
        Log.d(TAG, "Processing command: ${request.command}")

        return when (request.command.lowercase()) {
            Commands.DIAL -> {
                if (request.number.isNullOrEmpty()) {
                    CommandResponse(false, "Number is required for dial command")
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
                message = "Phone Manager is running"
            )
            else -> CommandResponse(
                success = false,
                message = "Unknown command: ${request.command}"
            )
        }
    }

    fun broadcastMessage(message: String) {
        Log.d(TAG, "Broadcasting: $message")
        broadcast(message)
    }
}