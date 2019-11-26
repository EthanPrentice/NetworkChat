package com.ethanprentice.networkchat.adt

import android.util.Log
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class ShakaSocket(private val cm: ConnectionManager, host: String, port: Int): Socket(host, port) {

    private var isReading = false

    override fun close() {
        isReading = false
        super.close()
    }

    fun read() {
        if (isReading) {
            return
        }

        thread(start = true) {
            val inStream = BufferedReader(InputStreamReader(inputStream))

            isReading = true
            while (isReading) {
                try {
                    val socketData = inStream.readLine()
                    Log.v(TAG, "Received data: $socketData (TCP port $localPort)")
                    if (socketData == null) {
                        cm.closeSocket(this)
                        break
                    }

                    val message: Message? = MessageRouter.msgFactory.getMessage(socketData)
                    if (message == null) {
                        Log.e(TAG, "$socketData is an invalid Message format!")
                    } else {
                        MessageRouter.handleMessage(message)
                    }
                }
                catch (e: SocketException) {
                    if (e.message == "Socket closed") {
                        cm.closeSocket(this)
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

    fun write(msg: SerializableMessage) {
        thread(start=true) {
            write(msg.toJsonString())
        }
    }

    private fun write(str: String) {
        thread(start = true) {
            val writer = PrintWriter(OutputStreamWriter(outputStream))

            try {
                Log.i(TAG, "Writing $str to ${inetAddress.hostAddress}:${port}")
                writer.println(str)
                writer.flush()
            }
            catch(e: Exception) {
                Log.e(TAG, e.message, e)
            }

        }
    }


    companion object {
        private val TAG = this::class.java.canonicalName
    }

}