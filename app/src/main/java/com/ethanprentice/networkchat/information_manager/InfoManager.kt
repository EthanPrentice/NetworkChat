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
    var groupInfo: GroupInfo? = null
        set(value) {
            field = value
            inGroup = (value != null)
        }

    var inGroup = false
        private set(value) {
            if (value == field) {
                return
            }
            field = value

            if (value) {
                for (listener in joinedGroupListeners) {
                    listener.run()
                }
            }
            else {
                for (listener in leftGroupListeners) {
                    listener.run()
                }
            }
        }


    private val joinedGroupListeners = ArrayList<Runnable>()
    private val leftGroupListeners = ArrayList<Runnable>()


    fun addJoinedGroupListener(action: Runnable) {
        joinedGroupListeners.add(action)
    }

    fun removeJoinedGroupListener(action: Runnable) {
        joinedGroupListeners.remove(action)
    }

    fun addLeftGroupListener(action: Runnable) {
        leftGroupListeners.add(action)
    }

    fun removeLeftGroupListener(action: Runnable) {
        leftGroupListeners.add(action)
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


}