package com.ethanprentice.networkchat.ui

import com.ethanprentice.networkchat.adt.Endpoint
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.ChatMessage
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse

object UiConfig {


    /* ENDPOINTS */
    val INFO_RSP_ENDPOINT = Endpoint("com.ethanprentice.shaka/ui/info-response", InfoResponse::class)
    val CHAT_BROADCAST_ENDPOINT = Endpoint("com.ethanprentice.shaka/ui/chat-message", ChatBroadcast::class)

}