package com.ethanprentice.networkchat.information_manager

import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.connection_manager.CmConfig
import com.ethanprentice.networkchat.information_manager.messages.UserInfoMessage
import com.ethanprentice.networkchat.message_router.EndpointManager

class ImMessageHandler(endpointManager: EndpointManager) : MessageHandler(endpointManager) {

    override val handlerName = "information-manager"

    override fun register() {
        endpointManager.registerHandler(this)
        endpointManager.registerEndpoint(ImConfig.UINFO_UPDATE_ENDPOINT)
    }

    override fun handleMessage(_message: Message) {
        when (_message.endpointName) {
            CmConfig.INFO_REQ_ENDPOINT.name -> updateUserInfo(_message as UserInfoMessage)
        }
    }

    private fun updateUserInfo(uInfoMsg: UserInfoMessage) {

    }
}