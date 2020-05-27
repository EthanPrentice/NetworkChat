package com.ethanprentice.networkchat.information_manager

import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.connection_manager.CmConfig
import com.ethanprentice.networkchat.information_manager.messages.UserInfoMessage
import com.ethanprentice.networkchat.message_router.EndpointManager
import java.lang.IllegalStateException

class ImMessageHandler(endpointManager: EndpointManager) : MessageHandler(endpointManager) {

    override val handlerName = "information-manager"

    override fun register() {
        endpointManager.registerHandler(this)
        endpointManager.registerEndpoint(ImConfig.UINFO_UPDATE_ENDPOINT)
    }

    override fun handleMessage(_message: Message) {
        when (_message.endpointName) {
            ImConfig.UINFO_UPDATE_ENDPOINT.name -> cacheUserInfo(_message as UserInfoMessage)
            else -> throw IllegalStateException("Invalid message for ImMessageHandler")
        }
    }

    /**
     * Caches user information for later use
     *
     * @param uInfoMsg Stores user information to be cached
     */
    private fun cacheUserInfo(uInfoMsg: UserInfoMessage) {
        InfoManager.cacheManager.cacheUserInfo(uInfoMsg.ip, uInfoMsg.uInfo)
    }
}