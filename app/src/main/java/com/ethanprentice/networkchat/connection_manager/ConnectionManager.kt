package com.ethanprentice.networkchat.connection_manager


import android.content.Context
import android.content.Intent
import android.util.Log
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.ShakaSocket
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.tasks.NetworkScanTask
import java.net.*


/**
 * Manages socket connections and streams
 *
 * @author Ethan Prentice
 */
object ConnectionManager {

    var isServer = false
        set(_isServer) {
            if (field == _isServer) return

            field = _isServer
            if (isServer) {
                closeClientSocket()
            }
            else {
                closeServerSockets()
            }
        }

    private val socketFactory = SocketFactory()

    private var clientSocket: ShakaSocket? = null
    private val tcpSockets = ArrayList<ShakaServerSocket>()

    private val socketCleanerThread = SocketCleanerThread(1000)


    init {
        socketCleanerThread.start()
        openUdpSocket()
    }


    /**
     * Opens a UDP socket
     */
    private fun openUdpSocket() {
        val intent = Intent(MainApp.context.applicationContext, UdpListenerService::class.java)
        MainApp.context.applicationContext.startService(intent)
    }


    /**
     * Closes the UDP socket, no reason to be called as of now
     */
    private fun closeUdpSocket() {
        val intent = Intent(MainApp.context, ConnectionManager::class.java)
        MainApp.context.stopService(intent)
    }


    /**
     * Opens a TCP socket returned by [socketFactory] and returns it
     * @return The created and [ShakaServerSocket]
     */
    fun openTcpSocket(): ShakaServerSocket {
        // TODO: don't throw error here, we are throwing error on wrong device.  Log as a warning instead.
        if (!isServer) {
            throw IllegalStateException("Cannot open a server port when device is acting as a client")
        }
        val socket = socketFactory.getServerSocket()
        socket.read()
        tcpSockets.add(socket)

        return socket
    }


    /**
     * Closes all server sockets and removes them from [tcpSockets]
     */
    private fun closeServerSockets() {
        for (socket in tcpSockets) {
            socket.close()
        }
        tcpSockets.clear()
    }

    fun closeSocket(socket: ShakaSocket) {
        if (socket == clientSocket) {
            clientSocket?.close()
            clientSocket = null
        }
    }

    fun closeSocket(socket: ShakaServerSocket) {
        tcpSockets.remove(socket)
        socket.close()
    }


    /**
     * Sends [msg] to the group host if in client mode, or all connected devices otherwise
     */
    fun writeToTcp(msg: SerializableMessage) {
        if (isServer) {
            for (socket in tcpSockets) {
                socket.write(msg)
            }
        }
        else {
            clientSocket?.write(msg)
        }
    }

    /**
     * Creates and opens [clientSocket] to connect to the ip and port provided
     * @param ip   The ip of the device to connect to
     * @param port The port of the server socket on the target device we want to connect to
     */
    fun openClientSocket(ip: String, port: Int) {
        if (isServer) {
            throw IllegalStateException("Cannot open a client port when device is acting as a server")
        }
        if (clientSocket != null) {
            Log.e(TAG, "Client socket must be closed before a new one can be opened")
            return
        }

        clientSocket = ShakaSocket(ip, port)
        clientSocket?.read()
    }


    /**
     * Closes [clientSocket] and sets it to null
     */
    private fun closeClientSocket() {
        clientSocket?.close()
        clientSocket = null
    }


    /**
     * Starts a task to scan the network for other devices running Shaka, sending relevant messages to MessageHandlers
     */
    fun scanNetwork() {
        NetworkScanTask(this).execute()
    }

    /**
     * Returns local IP of the device
     * @returns [InetAddress] of the current device
     */
    fun getDeviceIp(): InetAddress {
        val wm = MainApp.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipString = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

        return InetAddress.getByName(ipString)
    }

    /**
     * Closes all UDP, TCP server, and client sockets
     */
    fun closeAll() {
        closeUdpSocket()
        closeClientSocket()
        closeServerSockets()
    }


    /**
     * Returns a thread that will check for closed / unconnected sockets and remove them
     *
     * @param freqMs The frequency at which the check runs in milliseconds
     * @return The thread that will clean sockets
     */
    private class SocketCleanerThread(val freqMs: Long) : Thread({
        while (true) {
            val iter = tcpSockets.listIterator()
            while (iter.hasNext()) {
                val socket = iter.next()

                if (socket.isClosed) {
                    socket.close()
                    iter.remove()
                }
            }

            clientSocket.let {
                // clientSocket closed = disconnected from server
                // set socket to null and groupInfo to null
                if (it == null || it.isClosed) {
                    it?.close()
                    clientSocket = null
                    if (!isServer) {
                        InfoManager.groupInfo = null
                    }
                }
            }

            sleep(freqMs)
        }
    })


    private val TAG = ConnectionManager::class.java.canonicalName

}