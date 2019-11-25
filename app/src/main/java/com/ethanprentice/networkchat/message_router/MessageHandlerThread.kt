package com.ethanprentice.networkchat.message_router

import com.ethanprentice.networkchat.adt.Message
import java.util.*

class MessageHandlerThread : Thread() {
    val active = true
    private val messageQueue = LinkedList<Message>()

    fun queueMessage(message: Message) {
        messageQueue.add(message)
    }

    override fun run() {
        var msg: Message

        while (active) {
            while (!messageQueue.isEmpty()) {
                msg = messageQueue.pop()

                val handlers = MessageRouter.endpointManager.getHandlers(msg.endpointName)

                for (handler in handlers) {
                    handler.handleMessage(msg)
                }
            }
            sleep(100)
        }
    }
}