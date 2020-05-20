package com.ethanprentice.networkchat.message_router

import android.util.Log
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.connection_manager.CmMessageHandler
import com.ethanprentice.networkchat.information_manager.ImMessageHandler
import com.ethanprentice.networkchat.ui.UiMessageHandler

/**
 * Acts as a middleware so all components send messages to a single point and receive from a single point (other than messages received from external devices)
 * This reduces interdependencies between components
 *
 * @author Ethan Prentice
 */
object MessageRouter {

    // holds all of our registered MessageHandlers and their respective Endpoints
    val endpointManager = EndpointManager()

    // MessageHandlers
    private val cmMsgHandler = CmMessageHandler(MainApp.connManager, endpointManager)
    private val uiMsgHandler = UiMessageHandler(endpointManager)
    private val imMsgHandler = ImMessageHandler(endpointManager)

    val msgFactory = MessageFactory(this)

    private val handlerThread = MessageRouterThread()


    init {
        handlerThread.start()

        cmMsgHandler.register()
        uiMsgHandler.register()
        imMsgHandler.register()
    }

    /**
     * Redirects messages to their endpoint's MessageHandler
     * @param _message The message to be redirected
     */
    fun handleMessage(_message: Message) {
        Log.v(TAG, "received $_message")

        handlerThread.queueMessage(_message)
    }

    private val TAG = this::class.java.canonicalName
}