package com.ethanprentice.networkchat.connection_manager

import android.util.Log
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import java.io.IOException
import java.net.DatagramSocket

class SocketFactory {

    private val TAG = SocketFactory::class.java.canonicalName

    /**
     * Gets the next available UDP socket that is not in use
     * @returns a UDP socket on the next available port in the range
     */
    fun getDatagramSocket(): DatagramSocket {
        for (port in CmConfig.SERVER_PORT_RANGE) {
            try {
                return DatagramSocket(port)
            }
            catch (e: IOException) {
                Log.e(TAG, "Couldn't use prt $port")
                // that port was unavailable, try next one
                continue
            }
        }

        throw IllegalStateException("Could not find a free port in the specified range")
    }

    /**
     * Gets the next available TCP socket that is not in use
     * @returns a TCP socket on the next available port in the range
     */
    fun getServerSocket(): ShakaServerSocket {
        for (port in CmConfig.SERVER_PORT_RANGE) {
            try {
                return ShakaServerSocket(port)
            }
            catch (e: IOException) {
                // that port was unavailable, try next one
                continue
            }
        }

        throw IllegalStateException("Could not find a free port in the specified range")
    }

}