package com.ethanprentice.networkchat.connection_manager.messages

import android.graphics.Bitmap
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.enums.ConnType
import com.ethanprentice.networkchat.adt.serialization.BitmapSerializer
import com.ethanprentice.networkchat.connection_manager.CmConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json

/**
 * To be sent to a UDP socket on another device to request a TCP connection
 *
 * @param ip       The ip address of the current device
 * @param port     The port that the UDP socket is running on on the current device
 * @param connType [ConnType] string TODO: remove this, it is unnecessary
 * @param userDispName     The current users display userDispName to show the target device who is requesting a connection (optional)
 * @param userImg   The current users display image's url to show the target device who is requesting a connection (optional)
 *
 * @author Ethan Prentice
 */
@Serializable
data class ConnectionRequest  (
        val ip: String,
        val port: Int,
        val connType: String,
        val userDispName: String
) : SerializableMessage() {

    override var endpointName = CmConfig.CONN_REQ_ENDPOINT.name

    override fun toString(): String {
        return toJsonString()
    }

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): ConnectionRequest {
            return Json.parse(serializer(), jsonString)
        }
    }
}