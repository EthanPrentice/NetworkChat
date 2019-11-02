package com.ethanprentice.networkchat.connection_manager.messages

import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.UserInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
class ChatBroadcast(
        val ip: String,
        val chatMsg: String,
        val sender: UserInfo
) : SerializableMessage() {

    override var endpointName = "com.ethanprentice.shaka/ui/chat-message"

    override fun toJsonString(): String {
        return Json.stringify(serializer(), this)
    }

    companion object {
        fun getFromJsonString(jsonString: String): ChatBroadcast {
            return Json.parse(serializer(), jsonString)
        }
    }

}