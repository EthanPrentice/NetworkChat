package com.ethanprentice.networkchat.tasks

import android.os.AsyncTask
import android.util.Log
import com.ethanprentice.networkchat.connection_manager.CmConfig
import com.ethanprentice.networkchat.connection_manager.service.SocketListenerService
import com.ethanprentice.networkchat.connection_manager.messages.InfoRequest
import com.ethanprentice.networkchat.information_manager.InfoManager
import java.net.InetAddress
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

/**
 * Scans the network for devices also running Shaka by sending a [InfoRequest] over a UDP broadcast
 *
 * @author Ethan Prentice
 */
class NetworkScanTask : AsyncTask<Void, Void, Void>() {

    private fun sendInfoReq(socket: DatagramSocket, address: InetAddress, port: Int) {
        try {
            if (SocketListenerService.getUdpPort() == null) {
                synchronized(SocketListenerService.getUdpMonitor()) {
                    SocketListenerService.getUdpMonitor().wait()
                }
            }

            val req = InfoRequest(
                    InfoManager.deviceIp.hostAddress,
                    SocketListenerService.getUdpPort()!!
            )

            val buf = req.toJsonString().toByteArray()

            val packet = DatagramPacket(buf, buf.size, address, port)
            socket.send(packet)

        }
        catch (e: SocketException) {
            Log.e("Udp:", "Socket Error:", e)
        }
        catch (e: IOException) {
            Log.e("Udp Send:", "IO Error:", e)
        }
    }

    override fun doInBackground(vararg p0: Void?): Void? {

        Log.i(TAG, "Scanning for devices...")

        try {
            val udpSocket = DatagramSocket(0)
            udpSocket.broadcast = true

            val ipString = InfoManager.deviceIp.hostAddress
            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

            val address = InetAddress.getByName(prefix + "255")

            for (port in CmConfig.SERVER_PORT_RANGE) {
                sendInfoReq(udpSocket, address, port)
            }

        } catch (t: Throwable) {
            Log.e(TAG, "Error scanning network", t)
        }

        Log.i(TAG, "Done scanning for devices on the network")

        return null
    }

    companion object {
        private val TAG = this::class.java.canonicalName
    }

}