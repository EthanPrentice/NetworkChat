package com.ethanprentice.networkchat.connection_manager.service

import android.util.Log
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.ShakaSocket
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.ConnectionState
import com.ethanprentice.networkchat.information_manager.InfoManager

class TcpListener(private val cm: ConnectionManager) {
    private var clientSocket: ShakaSocket? = null
    private val tcpSockets = ArrayList<ShakaServerSocket>()

    private val socketCleanerThread = SocketCleanerThread(1000)

    init {
        socketCleanerThread.start()
    }

    fun setClientSocket(socket: ShakaSocket) {
        if (clientSocket != null) {
            Log.e(TAG, "Cannot set a new clientSocket until the current one is closed!")
            return
        }

        socket.read()
        clientSocket = socket
    }

    fun addServerSocket(socket: ShakaServerSocket) {
        socket.read()
        tcpSockets.add(socket)
    }

    fun closeServerSockets() {
        for (socket in tcpSockets) {
            socket.close()
        }
        tcpSockets.clear()
    }

    fun closeClientSocket() {
        clientSocket?.close()
        clientSocket = null
    }

    fun closeSocket(socket: ShakaSocket) {
        if (socket == clientSocket) {
            closeClientSocket()
        }
    }

    fun closeSocket(socket: ShakaServerSocket) {
        tcpSockets.remove(socket)
    }

    /**
     * Sends [msg] to the group host if in client mode, or all connected devices otherwise
     */
    fun writeToSockets(msg: SerializableMessage) {
        if (cm.isServer()) {
            for (socket in tcpSockets) {
                socket.write(msg)
            }
        }
        else if (cm.isClient()) {
            clientSocket?.write(msg)
        }
    }


    /**
     * Returns a thread that will check for closed / unconnected sockets and remove them
     *
     * @param freqMs The frequency at which the check runs in milliseconds
     * @return The thread that will clean sockets
     */
    private inner class SocketCleanerThread(val freqMs: Long) : Thread({
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
                    if (!cm.isServer()) {
                        cm.stateManager.setToUnconnected()
                        InfoManager.groupInfo = null
                    } else {
                        clientSocket = null
                    }
                }
            }

            sleep(freqMs)
        }
    })


    companion object {
        private val TAG = TcpListener::class.java.canonicalName
    }

}