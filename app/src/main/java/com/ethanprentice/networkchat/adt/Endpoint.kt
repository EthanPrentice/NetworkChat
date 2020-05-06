package com.ethanprentice.networkchat.adt

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Specified the [MessageHandler] that [Messages] can be redirected to as well as the Endpoint userDispName
 * and the [Message] associated with the [Endpoint]
 *
 * @author Ethan Prentice
 */
@Serializable
data class Endpoint(
        val name: String,
        val type: KClass<out Message>

) : Comparable<Endpoint> {

    override fun compareTo(other: Endpoint): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Endpoint) {
            name == other.name
        }
        else {
            false
        }
    }

    fun getEndpointInfo() : EndpointInfo {
        return EndpointInfo.fromEndpoint(this)
    }

}