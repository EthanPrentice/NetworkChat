package com.ethanprentice.networkchat.ui

import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.util.*


class UiMessageHandler(msgManager: MessageRouter) : MessageHandler(msgManager) {

    override val handlerName = "ui"

    // store messages in the queue if activity is paused
    private val handlerThread = UiMessageHandlerThread()

    init {
        handlerThread.start()
    }

    override fun register() {
        msgRouter.endpointManager.registerHandler(this)
        msgRouter.endpointManager.registerEndpoint(UiConfig.INFO_RSP_ENDPOINT)
        msgRouter.endpointManager.registerEndpoint(UiConfig.CHAT_BROADCAST_ENDPOINT)
    }

    override fun handleMessage(_message: Message) {
        if (MainApp.currActivity == null) {
            handlerThread.queueMessage(_message)
        }
        else {
            when (_message.endpointName) {
                UiConfig.INFO_RSP_ENDPOINT.name -> handleInfoResponse(_message as InfoResponse)
                UiConfig.CHAT_BROADCAST_ENDPOINT.name -> handleChatBroadcast(_message as ChatBroadcast)
            }
        }
    }

    private fun handleInfoResponse(infoRsp: InfoResponse) {
        MainApp.currActivity.let {
            if (it is ChatActivity) {
                it.controller.addDeviceInfo(infoRsp)
            }
        }
    }

    private fun handleChatBroadcast(chatBroadcast: ChatBroadcast) {
        val activity = MainApp.currActivity
        activity?.let {
            if (it is ChatActivity) {
                it.controller.addChatMsgView(chatBroadcast)
            }
        }
    }

    /**
     * If Activity is paused then we will queue messages until they are able to be handled
     *
     */
    private inner class UiMessageHandlerThread : Thread() {
        val active = true
        private val messageQueue = LinkedList<Message>()

        fun queueMessage(message: Message) {
            messageQueue.add(message)
        }

        override fun run() {
            var msg: Message

            while (active) {
                while (!messageQueue.isEmpty() && MainApp.currActivity != null) {
                    msg = messageQueue.pop()
                    handleMessage(msg)
                }
                sleep(100)
            }
        }
    }

}