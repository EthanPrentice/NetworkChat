package com.ethanprentice.networkchat.ui

import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.message_router.MessageRouter

class UiMessageHandler(msgManager: MessageRouter) : MessageHandler(msgManager) {

    override val handlerName = "ui"

    override fun register() {
        msgRouter.endpointManager.registerHandler(this)
        msgRouter.endpointManager.registerEndpoint(UiConfig.INFO_RSP_ENDPOINT)
        msgRouter.endpointManager.registerEndpoint(UiConfig.CHAT_BROADCAST_ENDPOINT)
    }

    override fun handleMessage(_message: Message) {
        when (_message.endpointName) {
            UiConfig.INFO_RSP_ENDPOINT.name -> handleInfoResponse(_message as InfoResponse)
            UiConfig.CHAT_BROADCAST_ENDPOINT.name -> handleChatBroadcast(_message as ChatBroadcast)
        }
    }

    fun handleInfoResponse(infoRsp: InfoResponse) {
        MainApp.currActivity.let {
            if (it is ChatActivity) {
                it.controller.addDeviceInfo(infoRsp)
            }
        }
    }

    fun handleChatBroadcast(chatBroadcast: ChatBroadcast) {
        val activity = MainApp.currActivity
        if (activity is ChatActivity) {
            activity.controller.addChatMsgView(chatBroadcast)
        }
    }

}