package com.ethanprentice.networkchat.information_manager.messages

import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.information_manager.ImConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
class UserInfoMessage(
        val ip: String,
        val uInfo: UserInfo
) : SerializableMessage() {

    override var endpointName = ImConfig.UINFO_UPDATE_ENDPOINT.name

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): UserInfoMessage {
            return Json.parse(serializer(), jsonString)
        }
    }
}