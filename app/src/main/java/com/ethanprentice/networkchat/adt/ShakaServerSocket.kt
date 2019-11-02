package com.ethanprentice.networkchat.adt

import android.util.Log
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread
import java.lang.Exception


/**
 * Class to handle operations and multithreading for [ServerSocket]s and their client [Socket]s
 * @param port The port to open the ServerSocket on
 *
 * @author Ethan Prentice
 */
class ShakaServerSocket(port: Int) : ServerSocket(port) {

    private var isReading = false
    private var clientSocket: Socket? = null
    private val acceptLock = Object()


    override fun close() {
        isReading = false
        clientSocket?.close()
        super.close()
    }

    override fun accept(): Socket? {
        soTimeout = ACCEPT_TIMEOUT

        try {
            thread(start = true) {
                try {
                    clientSocket = super.accept()
                    Log.i(TAG, "Accepted TCP connection on port $localPort with remote address ${clientSocket?.remoteSocketAddress}:${clientSocket?.port}")
                    synchronized(acceptLock) {
                        acceptLock.notifyAll()
                    }
                } catch (e: SocketException) {
                    clientSocket = null
                    Log.e(TAG, "SocketException: closing ServerSocket on port $localPort", e)
                    close()
                } catch (e: InterruptedIOException) {
                    clientSocket = null
                    Log.w(TAG, "No socket accepted in ${ACCEPT_TIMEOUT}ms.  Closing ServerSocket on port $localPort")
                    close()
                }
            }

        }
        catch(e: Exception) {
            Log.e(TAG, e.message, e)
        }

        return clientSocket
    }

    fun read() {
        thread(start = true) {

            synchronized(acceptLock) {
                if (clientSocket == null) {
                    Log.d(TAG, "Client has not been accepted. Waiting to read. (port: $localPort)")
                    acceptLock.wait()
                    Log.d(TAG, "Client has been accepted. Now reading from socket. (port: $localPort)")
                }
            }

            val inStream = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

            isReading = true
            while (isReading) {
                try {
                    val socketData = inStream.readLine()
                    Log.v(TAG, "Received data: $socketData (TCP port $localPort)")
                    if (socketData == null) {
                        ConnectionManager.closeSocket(this)
                        break
                    }

                    val message: Message? = MessageRouter.msgFactory.getMessage(socketData)
                    if (message == null) {
                        Log.e(TAG, "$message is an invalid Message format!")
                    } else {
                        MessageRouter.handleMessage(message)
                    }
                }
                catch (e: SocketException) {
                    if (e.message == "Socket closed") {
                        ConnectionManager.closeSocket(this)
                    }
                    else {
                        Log.e(TAG, e.message, e)
                    }
                }
                catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }
            }

        }
    }

    fun stopReading() {
        isReading = false
    }

    private fun write(str: String) {
        thread(start = true) {
            synchronized(acceptLock) {
                if (clientSocket == null) {
                    Log.d(TAG, "Client has not been accepted. Waiting to write. (port: $localPort)")
                    acceptLock.wait()
                    Log.d(TAG, "Client has been accepted. Now writing to socket. (port: $localPort)")
                }
            }

            val oStream = PrintWriter(OutputStreamWriter(clientSocket!!.getOutputStream()))
            try {
                Log.i(TAG, "Writing $str to ${clientSocket!!.inetAddress.hostAddress}:${clientSocket!!.port}")
                oStream.println(str)
                oStream.flush()
            }
            catch(e: Exception) {
                Log.e(TAG, e.message, e)
            }

        }
    }

    fun write(msg: SerializableMessage) {
        thread(start=true) {
            write(msg.toJsonString())
        }
    }

    fun isConnected(): Boolean? {
        return clientSocket?.isConnected
    }


    companion object {
        private val TAG = this::class.java.canonicalName

        private const val ACCEPT_TIMEOUT = 2000
    }

}