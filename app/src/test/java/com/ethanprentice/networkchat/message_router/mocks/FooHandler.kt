package com.ethanprentice.networkchat.message_router.mocks

import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.message_router.MessageRouter


class FooHandler(router: MessageRouter) : MessageHandler(router) {
    override val handlerName = "foo"

    override fun register() {}
    override fun handleMessage(_message: Message) {}
}