package com.ethanprentice.networkchat.adt

/**
 * More verbose information extracted from the [Endpoint] string
 *
 * @author Ethan Prentice
 */
data class EndpointInfo(
        val fullName: String,
        val app: String,
        val handler: String,
        val path: String
) {
    companion object {
        fun fromEndpoint(endpoint: Endpoint) : EndpointInfo {
            val delimiter = "/"
            val splitString = endpoint.name.split(delimiter, limit=3)

            if (splitString.size != 3) {
                throw IllegalStateException("Invalid endpointName structure!")
            }

            return EndpointInfo(endpoint.name, splitString[0], splitString[1], splitString[2])
        }

        fun fromString(str: String) : EndpointInfo {
            val delimiter = "/"
            val splitString = str.split(delimiter, limit=3)

            if (splitString.size != 3) {
                throw IllegalStateException("Invalid endpointName structure!")
            }

            return EndpointInfo(str, splitString[0], splitString[1], splitString[2])
        }

    }


    override fun toString(): String {
        return "EndpointInfo: app: $app, handler: $handler, path: $path"
    }
}