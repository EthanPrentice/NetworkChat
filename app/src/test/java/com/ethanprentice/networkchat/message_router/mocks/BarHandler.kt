package com.ethanprentice.networkchat.message_router.mocks

import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.message_router.EndpointManager


class BarHandler(eManager: EndpointManager) : MessageHandler(eManager) {
    override val handlerName = "bar"

    override fun register() {}
    override fun handleMessage(_message: Message) {}
}