package com.ethanprentice.networkchat.connection_manager.messages

import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.ui.UiConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * To be sent as a response to [InfoRequest] to notify the target device that the current device is also running Shaka
 *
 * @param ip     The ip of the current device
 * @param port   The port of the current device
 * @param name   The current user's display userDispName to show the target who owns the device
 * @param imgUrl The current user's display image's url to show the target who owns the device
 *
 * @author Ethan Prentice
 */
@Serializable
data class InfoResponse(
        val ip: String,
        val port: Int,
        val name: String,
        val imgUrl: String

) : SerializableMessage() {

    override var endpointName: String = UiConfig.INFO_RSP_ENDPOINT.name

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): InfoResponse {
            return Json.parse(serializer(), jsonString)
        }
    }
}