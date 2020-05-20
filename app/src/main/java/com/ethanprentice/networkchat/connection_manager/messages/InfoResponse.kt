package com.ethanprentice.networkchat.connection_manager.messages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.serialization.BitmapSerializer
import com.ethanprentice.networkchat.ui.UiConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * To be sent as a response to [InfoRequest] to notify the target device that the current device is also running Shaka
 *
 * @param ip     The ip of the current device
 * @param port   The port of the current device
 * @param groupName   The current user's display userDispName to show the target who owns the device
 * @param ownerName   The group owner's display name
 * @param ownerImg    The group owner's display image to show the target who owns the device
 *
 * @author Ethan Prentice
 */
@Serializable
data class InfoResponse(
        val ip: String,
        val port: Int,
        val groupName: String,
        val ownerName: String

//        @Serializable(with=BitmapSerializer::class)
//        var ownerImg: Bitmap

) : SerializableMessage() {

    override var endpointName: String = UiConfig.INFO_RSP_ENDPOINT.name

    override fun toJsonString(): String {
//        ownerImg = Bitmap.createScaledBitmap(ownerImg, 20, 20, false)
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): InfoResponse {
            return Json.parse(serializer(), jsonString)
        }
    }
}