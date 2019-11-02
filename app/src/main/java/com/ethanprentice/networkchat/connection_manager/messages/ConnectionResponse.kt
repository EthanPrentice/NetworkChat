package com.ethanprentice.networkchat.connection_manager.messages

import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.connection_manager.CmConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Used to to be sent to a UDP socket on a device that sent a [ConnectionRequest]
 *
 * @param ip       The ip address of the current device
 * @param port     The port that the UDP socket is running on on the current device
 * @param accepted Whether the [ConnectionRequest] was accepted by the current user or not
 * @param tcpPort  The TCP port for the client to connect to if accepted (null when [accepted] is false)
 *
 * @author Ethan Prentice
 */
@Serializable
class ConnectionResponse(
        val ip: String,
        val port: Int,
        val accepted: Boolean,
        val tcpPort: Int?,
        val groupInfo: GroupInfo?

) : SerializableMessage() {

    override var endpointName = CmConfig.CONN_RSP_ENDPOINT.name

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): ConnectionResponse {
            return Json.parse(serializer(), jsonString)
        }
    }
}