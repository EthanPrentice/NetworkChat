package com.ethanprentice.networkchat.connection_manager


import android.content.Intent
import android.util.Log
import android.os.Build
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.ShakaSocket
import com.ethanprentice.networkchat.connection_manager.service.UdpListenerService
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.tasks.NetworkScanTask


/**
 * Manages socket connections and streams
 *
 * @author Ethan Prentice
 */
object ConnectionManager {

    private val socketFactory = SocketFactory()
    val stateManager = CmStateManager(this)

    private var clientSocket: ShakaSocket? = null
    private val tcpSockets = ArrayList<ShakaServerSocket>()

    private val socketCleanerThread = SocketCleanerThread(1000)


    init {
        socketCleanerThread.start()
        openUdpSocket()
    }


    fun isServer(): Boolean {
        return stateManager.currentState == ConnectionState.SERVER
    }

    fun isConnected(): Boolean {
        return stateManager.currentState != ConnectionState.UNCONNECTED
    }

    fun isClient(): Boolean {
        return stateManager.currentState == ConnectionState.CLIENT
    }

    /**
     * Opens a UDP socket
     */
    fun openUdpSocket() {
        val intent = Intent(MainApp.context.applicationContext, UdpListenerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MainApp.context.applicationContext.startForegroundService(intent)
        } else {
            MainApp.context.applicationContext.startService(intent)
        }
    }


    /**
     * Closes the UDP socket
     */
    fun closeUdpSocket() {
        val intent = Intent(MainApp.context.applicationContext, UdpListenerService::class.java)
        MainApp.context.stopService(intent)
    }


    /**
     * Opens a TCP socket returned by [socketFactory] and returns it
     * @return The created and [ShakaServerSocket]
     */
    fun openTcpSocket(): ShakaServerSocket {
        check(stateManager.currentState == ConnectionState.SERVER) {
            "Cannot create ServerSockets when not acting as a server!"
        }

        val socket = socketFactory.getServerSocket()
        socket.read()
        tcpSockets.add(socket)

        return socket
    }


    /**
     * Closes all server sockets and removes them from [tcpSockets]
     */
    fun closeServerSockets() {
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
        if (stateManager.currentState == ConnectionState.SERVER) {
            for (socket in tcpSockets) {
                socket.write(msg)
            }
        }
        else if (stateManager.currentState == ConnectionState.CLIENT) {
            clientSocket?.write(msg)
        }
    }

    /**
     * Creates and opens [clientSocket] to connect to the ip and port provided
     * @param ip   The ip of the device to connect to
     * @param port The port of the server socket on the target device we want to connect to
     */
    fun openClientSocket(ip: String, port: Int) {
        check(stateManager.currentState == ConnectionState.CLIENT) {
            "Cannot open a client socket when not acting as a client"
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
    fun closeClientSocket() {
        clientSocket?.close()
        clientSocket = null
    }


    /**
     * Starts a task to scan the network for other devices running Shaka, sending relevant messages to MessageHandlers
     */
    fun scanNetwork() {
        NetworkScanTask().execute()
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

            clientSocket?.let {
                // clientSocket closed = disconnected from server
                if (it.isClosed) {
                    if (!isServer()) {
                        stateManager.setToUnconnected()
                        InfoManager.groupInfo = null
                    } else {
                        clientSocket = null
                    }
                }
            }

            sleep(freqMs)
        }
    })


    private val TAG = ConnectionManager::class.java.canonicalName

}