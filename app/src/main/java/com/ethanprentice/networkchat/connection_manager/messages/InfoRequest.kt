package com.ethanprentice.networkchat.connection_manager.messages

import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.connection_manager.CmConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * To be sent as a broadcast to find devices on the network also running Network Chat
 *
 * @param ip   The ip of the current device
 * @param port The port of the current device
 *
 * @author Ethan Prentice
 */
@Serializable
data class InfoRequest(
        val ip: String,
        val port: Int

) : SerializableMessage() {

    override var endpointName = CmConfig.INFO_REQ_ENDPOINT.name

    override fun toString(): String {
        return toJsonString()
    }

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): InfoRequest {
            return Json.parse(serializer(), jsonString)
        }
    }
}