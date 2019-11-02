package com.ethanprentice.networkchat.tasks

import android.os.AsyncTask
import android.util.Log
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.SerializableMessage
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


/**
 * Sends a [Message] over UDP to the [address] and [port]
 *
 * @param address The IP address to send the message to
 * @param port    The port to send the [Message] to at the target ip address
 * @param message The [Message] to be sent to the other UDP socket
 *
 * @author Ethan Prentice
 */
class SendUdpMessage(
        private val address: InetAddress,
        private val port: Int,
        private val message: SerializableMessage

) : AsyncTask<Void, Void, Unit>() {

    override fun doInBackground(vararg p0: Void?) {

        Log.i(TAG, "Scanning for devices...")

        try {
            val udpSocket = DatagramSocket(0)

            val buf = message.toJsonString().toByteArray()

            val packet = DatagramPacket(buf, buf.size, address, port)
            udpSocket.send(packet)

        }
        catch (t: Throwable) {
            Log.e(TAG, "Error sending connection request", t)
        }
    }

    companion object {
        private val TAG = this::class.java.canonicalName
    }

}