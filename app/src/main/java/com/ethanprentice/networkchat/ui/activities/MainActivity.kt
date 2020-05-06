package com.ethanprentice.networkchat.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.ui.activities.chat_activity.frags.scan_network.ScanNetworkFragment
import com.ethanprentice.networkchat.adt.ShakaActivity
import com.ethanprentice.networkchat.ui.activities.login_activity.LoginActivity
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : ShakaActivity(), ScanNetworkFragment.ScanNetworkFragListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

        val userRemembered = sharedPrefs.getBoolean("rememberUser", false)
        val dispName = sharedPrefs.getString("userDisplayName", null)
        if (userRemembered && dispName != null) {

            // TODO: replace with cache manager when implemented
            val cachePath = cacheDir?.absolutePath
            val filename = "user_disp_img"
            val file = File(cachePath, filename)
            val bmp = BitmapFactory.decodeFile(file.path)
            InfoManager.userInfo = UserInfo(dispName, bmp)

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
