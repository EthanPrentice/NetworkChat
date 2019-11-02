package com.ethanprentice.networkchat.message_router

import android.util.Log
import com.ethanprentice.networkchat.adt.Endpoint
import com.ethanprentice.networkchat.adt.EndpointInfo
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.MessageHandler
import java.util.*


/**
 * Allows the dynamic registering of MessageHandlers and Endpoints so MessageRouter is not directly dependent on any components
 * This also helps to allow each MessageHandler to operate completely independently of any components in-case of addition of new components or deprecation
 *
 * @author Ethan Prentice
 */
class EndpointManager {

    private val endpointMap = TreeMap<MessageHandler, ArrayList<Endpoint>>()

    /**
     * Registers a [MessageHandler] so that [MessageRouter] knows it exists and can route messages to it
     * @param handler The MessageHandler to register
     */
    fun registerHandler(handler: MessageHandler) {
        if (!endpointMap.containsKey(handler)) {
            endpointMap[handler] = ArrayList()
        }
        else {
            Log.w(TAG, "${handler.handlerName} is already registered")
        }
    }


    /**
     * Registers an [Endpoint] to a [MessageHandler] so that [Message]s can be routed through the handler to the [Endpoint]
     * @param endpoint The [Endpoint] to register
     */
    fun registerEndpoint(endpoint: Endpoint) {
        for (mHandler in endpointMap.keys) {
            if (mHandler.handlerName == endpoint.getEndpointInfo().handler) {
                endpointMap[mHandler]!!.add(endpoint)
                return
            }
        }
        Log.e(TAG, "Error registering ${endpoint.name}.  Must register the associated message handler before endpoints can be added to it")
    }


    /**
     * Gets the handler associated with the provided [endpoint]
     * @param endpoint The endpointName whose handler we need to return
     * @return the handler of the provided endpointName
     */
    fun getHandler(endpoint: Endpoint): MessageHandler? {
        return getHandler(endpoint.name)
    }

    /**
     * Gets the handler associated with the provided [endpointName]
     * @param endpointName The name of the [Endpoint] whose [MessageHandler] we need to return
     * @return the handler of the [Endpoint] with name [endpointName]
     */
    fun getHandler(endpointName: String): MessageHandler? {
        val handlerName = EndpointInfo.fromString(endpointName).handler
        val handlerNode = endpointMap.keys.filter { it.handlerName == handlerName }

        return if (handlerNode.isEmpty()) {
            Log.e(TAG, "MessageHandler $handlerName is not registered or does not exist")
            null
        }
        else {
            handlerNode[0]
        }
    }

    /**
     * Gets the handler associated with the provided [endpointName]
     * @param endpointName The name of the [Endpoint] to return
     * @return the [Endpoint] with the name equal to [endpointName], if none is found, returns null
     */
    fun getEndpoint(endpointName: String): Endpoint? {
        val eInfo = EndpointInfo.fromString(endpointName)

        var handlerEndpoints: ArrayList<Endpoint>? = null

        for (mHandler in endpointMap.keys) {
            if (mHandler.handlerName == eInfo.handler) {
                handlerEndpoints = endpointMap[mHandler]
            }
        }

        if (handlerEndpoints == null) {
            return null
        }

        for (endpoint in handlerEndpoints) {
            if (endpoint.name == endpointName) {
                return endpoint
            }
        }

        return null
    }


    companion object {
        private val TAG = this::class.java.canonicalName
    }

}