package com.ethanprentice.networkchat.connection_manager

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.net.DatagramPacket
import java.net.DatagramSocket


/**
 *  Listens for requests over UDP from clients that haven't been given a TCP socket yet
 *  This [Service] should never be run more than once in parallel
 *
 *  @author Ethan Prentice
 */
class UdpListenerService : Service() {

    private val socketFactory = SocketFactory()

    private lateinit var socket: DatagramSocket


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnVal = super.onStartCommand(intent, flags, startId)

        // Ensures that this Service will only ever have one instance running
        if (active) {
            Log.w(TAG, "UdpListenerService is already running. To reinitialize stop then start again.")
            stopSelf()
            return returnVal
        }

        active = true

        socket = socketFactory.getDatagramSocket()
        port = socket.localPort

        Log.i(TAG, "Started UdpListenerService (${ConnectionManager.getDeviceIp()}) on port $port")


        val packetReceiverThread = Thread(PacketReceiver(socket))
        packetReceiverThread.start()

        return returnVal
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroying UdpListenerService")
        active = false
        port = null
        socket.close()

        super.onDestroy()
    }


    /**
     * Container Runnable for receiving packets
     *
     * @param _ds The UDP socket to receive packets on
     */
    private inner class PacketReceiver(_ds: DatagramSocket) : Runnable {
        private val ds = _ds
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
                ds.receive(dp)

                val strMessage = String(byteMessage, 0, dp.length)
                Log.d(TAG, "Message received: $strMessage")

                // Came from ourselves, likely a self-broadcast
                if (dp.address == ConnectionManager.getDeviceIp()) {
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
        private val TAG = UdpListenerService::class.java.canonicalName

        /** port that the UDP socket is open on.  When set to a non-null value lockObj is notified */
        var port: Int? = null
            private set(_port) {
                field = _port
                if (port != null) {
                    synchronized(lockObj) {
                        lockObj.notify()
                    }
                }
            }

        var active: Boolean = false
            private set

        val lockObj = Object()
    }

}