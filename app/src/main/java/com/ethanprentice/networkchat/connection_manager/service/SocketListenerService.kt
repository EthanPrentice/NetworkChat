package com.ethanprentice.networkchat.connection_manager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import java.util.concurrent.locks.ReentrantLock


/**
 *  Listens for requests over UDP from clients that haven't been given a TCP socket yet
 *  This [Service] should never be run more than once in parallel
 *
 *  @author Ethan Prentice
 */
class SocketListenerService : Service() {

    lateinit var cm: ConnectionManager

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Creating SocketListenerService")
        startForeground(FG_SERVICE_ID, getForegroundNotification())

        cm = MainApp.connManager
        tcpListener = TcpListener(cm)
        udpListener = UdpListener(cm)

        if (!cm.isClient()) {
            cm.openUdpSocket()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Started SocketListenerService")

        if (!udpListener.active && !cm.isClient()) {
            udpListener.start()
        }

        return START_NOT_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        stopForeground(true)
        stopSelf()

        udpListener.stop()

        return super.stopService(name)
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroying SocketListenerService")
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

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = createNotificationChannel(NOTIF_CHANNEL_ID, "Services")
            NotificationCompat.Builder(this, channel)
        }
        else {
            NotificationCompat.Builder(this)
        }

        builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Network Chat")
                .setContentText("Socket Listener Service is running")
                .setContentIntent(pendingIntent)

        return builder.build()
    }


    companion object {

        private val TAG = SocketListenerService::class.java.canonicalName

        private const val FG_SERVICE_ID = 1
        private const val NOTIF_CHANNEL_ID = "services"

        lateinit var udpListener: UdpListener
            private set

        lateinit var tcpListener: TcpListener
            private set

        fun getUdpPort(): Int? {
            return udpListener.port
        }

        fun udpIsRunning(): Boolean {
            return udpListener.active
        }

        fun getUdpMonitor(): Object {
            return UdpListener.monitor
        }

    }

}