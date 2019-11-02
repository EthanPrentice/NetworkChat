package com.ethanprentice.networkchat.activities.chat_activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.chat_activity.ui.chat.ChatFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.group_settings.GroupSettingsFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.members.MembersFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.scan_network.ScanNetworkFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.settings.SettingsFragment
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.ShakaActivity
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.ChatMessage
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.ui.frags.CreateGroupFragment
import com.ethanprentice.networkchat.ui.views.ChatMessageView

class ChatActivity : ShakaActivity(), ScanNetworkFragment.ScanNetworkFragListener, CreateGroupFragment.CreateGroupFragListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var controller: ChatController

    private val onGroupJoinListener = Runnable {
        inGroup()
    }

    private val onGroupLeftListener = Runnable {
        notInGroup()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = ChatController(this)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_scan_network, R.id.nav_chat, R.id.nav_members, R.id.nav_group_settings, R.id.nav_settings
        ), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        InfoManager.addJoinedGroupListener(onGroupJoinListener)
        InfoManager.addLeftGroupListener(onGroupLeftListener)

        if (InfoManager.inGroup) {
            inGroup()
        }
        else {
            notInGroup()
        }

    }

    override fun onDestroy() {
        InfoManager.removeJoinedGroupListener(onGroupJoinListener)
        InfoManager.removeLeftGroupListener(onGroupLeftListener)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onCreateGroup(frag: CreateGroupFragment, gInfo: GroupInfo) {
        supportFragmentManager
                .beginTransaction()
                .remove(frag)
                .commit()

        ConnectionManager.isServer = true
        InfoManager.groupInfo = gInfo
    }


    fun inGroup() {
        controller.showNavDrawerItem(R.id.nav_chat)
        controller.showNavDrawerItem(R.id.nav_group_settings)
        controller.showNavDrawerItem(R.id.nav_members)
        controller.hideNavDrawerItem(R.id.nav_scan_network)
    }

    fun notInGroup() {
        controller.showNavDrawerItem(R.id.nav_scan_network)
        controller.hideNavDrawerItem(R.id.nav_chat)
        controller.hideNavDrawerItem(R.id.nav_group_settings)
        controller.hideNavDrawerItem(R.id.nav_members)
    }

}
