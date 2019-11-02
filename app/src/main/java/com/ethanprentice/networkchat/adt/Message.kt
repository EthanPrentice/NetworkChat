package com.ethanprentice.networkchat.adt

import kotlinx.serialization.*

/**
 * Base class for Messages sent / received internally and externally
 * Is [Serializable] since [SerializableMessage] inherits this and it must also be [Serializable]
 *
 * @author Ethan Prentice
 */
@Serializable
abstract class Message {
    abstract var endpointName: String

    fun getEndpointInfo(): EndpointInfo {
        return EndpointInfo.fromString(endpointName)
    }
}