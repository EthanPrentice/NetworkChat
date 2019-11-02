package com.ethanprentice.networkchat.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.ui.frags.CreateGroupFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.scan_network.ScanNetworkFragment
import com.ethanprentice.networkchat.adt.ShakaActivity


class MainActivity : ShakaActivity(), ScanNetworkFragment.ScanNetworkFragListener {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getPreferences(Context.MODE_PRIVATE)

        val dispName = prefs.getString("display_name", null)
        if (dispName != null) {
            InfoManager.userInfo = UserInfo(dispName, null)

            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /* End of frag interface methods */


    /**
     * Adds a DeviceInfoView to the LinearLayout using data included in the InfoResponse
     */
    fun addDeviceInfo(infoRsp: InfoResponse) {
        val frag = supportFragmentManager.findFragmentById(R.id.login_frame_container)
        if (frag != null && frag is ScanNetworkFragment) {
            runOnUiThread {
                frag.addDeviceInfo(infoRsp)
            }
        }
    }
}
