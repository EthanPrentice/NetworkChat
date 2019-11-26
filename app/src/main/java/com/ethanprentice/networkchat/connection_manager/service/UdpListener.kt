package com.ethanprentice.networkchat.connection_manager.service

import android.util.Log
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.SocketFactory
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException


class UdpListener(cm: ConnectionManager) {

    var active = false
        private set

    var port: Int? = null
        private set

    private val socketFactory = SocketFactory(cm)

    private lateinit var socket: DatagramSocket


    fun start() {
        if (!active) {
            active = true
            socket = socketFactory.getDatagramSocket()
            port = socket.localPort

            synchronized(monitor) {
                monitor.notify()
            }

            val packetReceiverThread = Thread(PacketReceiver(socket))
            packetReceiverThread.start()
        }
        else {
            Log.i(TAG, "Tried to start UdpListener but UdpListener is already started!")
        }
    }

    fun stop() {
        active = false
        socket.close()
        port = null
    }


    /**
     * Container Runnable for receiving packets
     *
     * @param ds The UDP socket to receive packets on
     */
    private inner class PacketReceiver(val ds: DatagramSocket) : Runnable {
        override fun run() {
            receivePackets(ds)
        }
    }


    /**
     * Listens for packets over UDP, constructing and sending messages to MessageRouter
     *
     * @param ds The UDP socket to receive packets on
     */
    private fun receivePackets(ds: DatagramSocket) {
        val byteMessage = ByteArray(4096)
        val dp = DatagramPacket(byteMessage, byteMessage.size)

        ds.broadcast = true

        try {
            while (active) {
                try {
                    ds.receive(dp)
                }
                catch(e: SocketException) {
                    if (active || !ds.isClosed) {
                        Log.e(TAG, "Unexpected SocketException!", e)
                    }
                    break
                }

                val strMessage = String(byteMessage, 0, dp.length)
                Log.d(TAG, "Message received: $strMessage")

                // Came from ourselves, likely a self-broadcast
                if (dp.address == InfoManager.getDeviceIp()) {
                    Log.d(TAG, "Message from self, skipping.")
                    continue
                }

                val message : Message? = MessageRouter.msgFactory.getMessage(strMessage)
                if (message == null) {
                    Log.e(TAG, "$strMessage is an invalid Message format!")
                }
                else {
                    MessageRouter.handleMessage(message)
                }

            }

        }
        catch (e: Exception) {
            Log.e(TAG, "Error receiving UDP messages", e)
        }
        finally {
            ds.close()
        }
    }


    companion object {
        private val TAG = UdpListener::class.java.canonicalName
        val monitor = Object()
    }

}