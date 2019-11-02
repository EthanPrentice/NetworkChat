package com.ethanprentice.networkchat.message_router

import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.EndpointInfo
import com.ethanprentice.networkchat.adt.Endpoint
import com.ethanprentice.networkchat.connection_manager.messages.*
import org.json.JSONObject


class MessageFactory(private val msgManager: MessageRouter) {

    /**
     * Gets a message based on the [Endpoint] it was sent to. ([Endpoint]s have a N:1 relationship to [Message] subclasses)
     * @return null if no [Message] subclass can be decided on, otherwise returns a [Message]
     */
    fun getMessage(messageString: String): Message? {
        val jsonMsg = JSONObject(messageString)
        if (!jsonMsg.has("endpointName")) {
            return null
        }
        val eInfo = EndpointInfo.fromString(jsonMsg.getString("endpointName"))

        val endpoint = msgManager.endpointManager.getEndpoint(eInfo.fullName)

        return when (endpoint?.type) {
            InfoRequest::class          -> InfoRequest.getFromJsonString(messageString)
            InfoResponse::class         -> InfoResponse.getFromJsonString(messageString)
            ConnectionRequest::class    -> ConnectionRequest.getFromJsonString(messageString)
            ConnectionResponse::class   -> ConnectionResponse.getFromJsonString(messageString)
            ChatMessage::class          -> ChatMessage.getFromJsonString(messageString)
            ChatBroadcast::class        -> ChatBroadcast.getFromJsonString(messageString)
            else -> null
        }
    }


}