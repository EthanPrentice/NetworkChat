package com.ethanprentice.networkchat.connection_manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.message_router.MessageRouter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import com.ethanprentice.networkchat.R


/**
 *  Listens for requests over UDP from clients that haven't been given a TCP socket yet
 *  This [Service] should never be run more than once in parallel
 *
 *  @author Ethan Prentice
 */
class UdpListenerService : Service() {

    private val socketFactory = SocketFactory()

    private lateinit var socket: DatagramSocket


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Creating UdpListenerService")
        startForeground(1, getForegroundNotification())
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnVal = super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "Starting UdpListenerService")

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

        return START_NOT_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        stopForeground(true)
        stopSelf()
        return super.stopService(name)
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroying UdpListenerService")
        active = false
        port = null
        socket.close()
        stopForeground(true)

        super.onDestroy()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getForegroundNotification(): Notification {
        // TODO: definitely change this, just a test
        val pendingIntent: PendingIntent = Intent(this, ChatActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = createNotificationChannel(NOTIF_CHANNEL_ID, "Services")

            NotificationCompat.Builder(this, channel)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Network Chat")
                    .setContentText("UDP Listener Service is running")
                    .setContentIntent(pendingIntent)
                    .build()
        }
        else {
            NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Network Chat")
                    .setContentText("UDP Listener Service is running")
                    .setContentIntent(pendingIntent)
                    .build()
        }
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

        private const val FG_SERVICE_ID = 1
        private const val NOTIF_CHANNEL_ID = "services"

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