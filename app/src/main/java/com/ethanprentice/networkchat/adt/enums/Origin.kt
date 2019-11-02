package com.ethanprentice.networkchat.adt.enums

/**
 * The origin of a [Message] to know whether it needs to be serialized / deserialized
 */
enum class Origin(private val strValue: String) {
    EXTERNAL("external"),
    INTERNAL("internal");
}