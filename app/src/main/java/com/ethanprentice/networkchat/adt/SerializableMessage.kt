package com.ethanprentice.networkchat.adt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * Base class for [Messages] sent / received from external sources
 * TODO: Add custom serializers to SerializableMessages to improve polymorphism (can't have vals in constructor here or else it won't be default serializable)
 *
 */
@Serializable
abstract class SerializableMessage: Message() {
    abstract fun toJsonString(): String

    companion object {
        fun getFromJsonString(jsonString: String): Message {
            return Json.parse(serializer(), jsonString)
        }
    }
}