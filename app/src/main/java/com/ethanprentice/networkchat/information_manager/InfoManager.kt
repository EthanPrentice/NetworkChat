package com.ethanprentice.networkchat.information_manager

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.UserInfo
import java.net.InetAddress

object InfoManager {

    var userInfo: UserInfo = UserInfo("None", null)

    var groupInfo: GroupInfo?
        get() = grpManager.groupInfo
        set(value) {
            grpManager.groupInfo = value
        }

    val grpManager = GroupManager()


    /**
     * Returns local IP of the device
     * @returns [InetAddress] of the current device
     */
    fun getDeviceIp(): InetAddress {
        val wm = MainApp.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipString = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

        return InetAddress.getByName(ipString)
    }


}