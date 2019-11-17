package com.ethanprentice.networkchat.message_router

import android.util.Log
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.connection_manager.CmMessageHandler
import com.ethanprentice.networkchat.ui.UiMessageHandler
import java.util.*
import kotlin.concurrent.thread

/**
 * Acts as a middleware so all components send messages to a single point and receive from a single point (other than messages received from external devices)
 * This reduces interdependencies between components
 *
 * @author Ethan Prentice
 */
object MessageRouter {

    // MessageHandlers
    private val cmMsgHandler = CmMessageHandler(this)
    private val uiMsgHandler = UiMessageHandler(this)

    // holds all of our registered MessageHandlers and their respective Endpoints
    val endpointManager = EndpointManager()

    val msgFactory = MessageFactory(this)

    private val messageQueue = LinkedList<Message>()

    private val handlerThread = MessageHandlerThread()


    init {
        handlerThread.start()

        cmMsgHandler.register()
        uiMsgHandler.register()
    }

    /**
     * Redirects messages to their endpoint's MessageHandler
     * @param _message The message to be redirected
     */
    fun handleMessage(_message: Message) {
        Log.v(TAG, "received $_message")

        messageQueue.add(_message)
    }


    private class MessageHandlerThread : Thread() {
        val active = true

        override fun run() {
            var msg: Message

            while (active) {
                while (!messageQueue.isEmpty()) {
                    msg = messageQueue.pop()

                    val handler = endpointManager.getHandler(msg.endpointName)
                    handler?.handleMessage(msg)
                }
                sleep(100)
            }
        }
    }


    private val TAG = this::class.java.canonicalName

}