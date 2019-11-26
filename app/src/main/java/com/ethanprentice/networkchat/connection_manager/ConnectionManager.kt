package com.ethanprentice.networkchat.connection_manager


import android.content.Intent
import android.util.Log
import android.os.Build
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.ShakaSocket
import com.ethanprentice.networkchat.connection_manager.service.SocketListenerService
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.tasks.NetworkScanTask
import kotlin.concurrent.thread


/**
 * Manages socket connections and streams
 *
 * @author Ethan Prentice
 */
class ConnectionManager {

    private val socketFactory = SocketFactory(this)
    val stateManager = CmStateManager(this)

    init {
        val intent = Intent(MainApp.context.applicationContext, SocketListenerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MainApp.context.applicationContext.startForegroundService(intent)
        } else {
            MainApp.context.applicationContext.startService(intent)
        }
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


    fun openUdpSocket() = SocketListenerService.udpListener.start()
    fun closeUdpSocket() = SocketListenerService.udpListener.stop()



    /**
     * Opens a TCP socket returned by [socketFactory] and returns it
     * @return The created [ShakaServerSocket]
     */
    fun openTcpSocket(): ShakaServerSocket {
        check(stateManager.currentState == ConnectionState.SERVER) {
            "Cannot create ServerSockets when not acting as a server!"
        }

        val socket = socketFactory.getServerSocket()
        SocketListenerService.tcpListener.addServerSocket(socket)

        return socket
    }


    /**
     * Creates and opens a client socket to connect to the ip and port provided
     * @param ip   The ip of the device to connect to
     * @param port The port of the server socket on the target device we want to connect to
     */
    fun openClientSocket(ip: String, port: Int) {
        check(stateManager.currentState == ConnectionState.CLIENT) {
            "Cannot open a client socket when not acting as a client"
        }

        val socket = ShakaSocket(this, ip, port)
        SocketListenerService.tcpListener.setClientSocket(socket)
    }


    fun closeServerSockets() = SocketListenerService.tcpListener.closeServerSockets()

    fun closeSocket(socket: ShakaSocket) = SocketListenerService.tcpListener.closeSocket(socket)

    fun closeSocket(socket: ShakaServerSocket) = SocketListenerService.tcpListener.closeSocket(socket)

    fun closeClientSocket() = SocketListenerService.tcpListener.closeClientSocket()


    /**
     * Sends [msg] to the group host if in client mode, or all connected devices otherwise
     */
    fun writeToTcp(msg: SerializableMessage) = SocketListenerService.tcpListener.writeToSockets(msg)


    /**
     * Closes all UDP, TCP server, and client sockets
     */
    fun closeAll() {
        closeUdpSocket()
        closeClientSocket()
        closeServerSockets()
    }


    private val TAG = ConnectionManager::class.java.canonicalName

}