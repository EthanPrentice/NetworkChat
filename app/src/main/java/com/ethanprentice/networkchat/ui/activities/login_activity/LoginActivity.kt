package com.ethanprentice.networkchat.ui.activities.login_activity

import android.content.Intent
import android.os.Bundle
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.ShakaActivity
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.information_manager.InfoManager


class LoginActivity : ShakaActivity(), LoginFragment.OnLoginListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.login_frame_container, LoginFragment.newInstance())
                .commit()
    }

    /* Frag interface methods */
    override fun onLogin(userInfo: UserInfo) {
        InfoManager.userInfo = userInfo
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }
}
