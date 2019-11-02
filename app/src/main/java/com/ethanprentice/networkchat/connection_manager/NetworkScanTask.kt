package com.ethanprentice.networkchat.connection_manager

import android.os.AsyncTask
import android.util.Log
import com.ethanprentice.networkchat.connection_manager.messages.InfoRequest
import java.net.InetAddress
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException


class NetworkScanTask : AsyncTask<Void, Void, Void>() {

    private val TAG = "NetScanTask"


    private fun sendInfoReq(socket: DatagramSocket, address: InetAddress, port: Int) {
        try {
            if (UdpListenerService.port == null) {
                synchronized(UdpListenerService.lockObj) {
                    UdpListenerService.lockObj.wait()
                }
            }

            val req = InfoRequest(
                    ConnectionManager.getDeviceIp().hostAddress,
                    UdpListenerService.port!!
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

            val ipString = ConnectionManager.getDeviceIp().hostAddress
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

}