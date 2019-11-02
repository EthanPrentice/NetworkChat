package com.ethanprentice.networkchat.adt

import com.ethanprentice.networkchat.adt.enums.Origin
import kotlinx.serialization.Serializable


@Serializable
class Callback {

    lateinit var origin: Origin

    // Should be null when origin is INTERNAL, non-null otherwise
    var ip: String? = null
    var port: Int? = null

    // Should be null when origin EXTERNAL, non-null otherwise
    var endpoint: Endpoint? = null

    companion object {

        fun getExternal(_ip: String, _port: Int): Callback {
            val cb = Callback()
            cb.origin = Origin.EXTERNAL
            cb.ip = _ip
            cb.port = _port
            return cb
        }

        fun getInternal(_endpoint: Endpoint): Callback {
            val cb = Callback()
            cb.origin = Origin.EXTERNAL
            cb.endpoint = _endpoint
            return cb
        }

    }

}