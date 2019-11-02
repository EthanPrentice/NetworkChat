package com.ethanprentice.networkchat.adt

import com.ethanprentice.networkchat.message_router.MessageRouter


/**
 * Represents a receiver / handler that MessageRouter can send messages to
 * This keeps the message logic within components and allows the MessageRouter to dynamically register handlers / endpoints
 *
 * @property handlerName Used to compare handlers to prevent registering duplicates and for processing endpointName strings
 */
abstract class MessageHandler(protected val msgRouter: MessageRouter) : Comparable<MessageHandler> {

    abstract val handlerName: String

    /**
     * Registers the MessageHandler and it's Endpoints with MessageRouter
     */
    abstract fun register()

    /**
     * Receives messages from the MessageRouter to be processed by the component
     * @param _message The message sent to the handler by MessageRouter
     */
    abstract fun handleMessage(_message: Message)

    /**
     * Compares the [MessageHandler]'s based on [handlerName] equivalence
     */
    override fun compareTo(other: MessageHandler): Int {
        return handlerName.compareTo(other.handlerName)
    }

    /**
     * @return true if [handlerName]'s are equal, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        return if (other is MessageHandler) {
            handlerName == other.handlerName
        }
        else {
            false
        }
    }

}