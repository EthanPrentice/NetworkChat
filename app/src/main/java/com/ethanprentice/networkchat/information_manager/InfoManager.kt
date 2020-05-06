package com.ethanprentice.networkchat.information_manager

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.tasks.DownloadImageTask
import java.net.InetAddress
import kotlin.concurrent.thread

object InfoManager {

    init {
        thread(start=true) {
            val wm = MainApp.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipString = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
            deviceIp = InetAddress.getByName(ipString)
        }
    }

    var userInfo: UserInfo = UserInfo("None", null)

    var groupInfo: GroupInfo?
        get() = grpManager.groupInfo
        set(value) {
            grpManager.groupInfo = value
        }

    val grpManager = GroupManager()

    lateinit var deviceIp: InetAddress

    fun getDpFactor(context: Context): Float {
        return context.resources.displayMetrics.density
    }


    fun getUserImage(context: Context) : Drawable {
        if (userInfo.imageBmp != null) {
            return BitmapDrawable(context.resources, userInfo.imageBmp)
        }
        else {
            val drawable = context.resources.getDrawable(R.drawable.ic_account_circle, null)
            drawable.setTint(context.resources.getColor(R.color.textColorSecondary, null))
            return drawable
        }
    }

}