package com.ethanprentice.networkchat.connection_manager.messages

import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.CmConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
class ChatMessage(
        val ip: String,
        val chatText: String,
        val sender: UserInfo

) : SerializableMessage() {

    override var endpointName = CmConfig.CHAT_MESSAGE.name

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): ChatMessage {
            return Json.parse(serializer(), jsonString)
        }
    }
}