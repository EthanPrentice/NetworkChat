package com.ethanprentice.networkchat.ui.activities.chat_activity

import android.content.Context
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.frags.scan_network.ScanNetworkFragment
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.ShakaActivity
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.ui.frags.CreateGroupFragment

class ChatActivity : ShakaActivity(), ScanNetworkFragment.ScanNetworkFragListener, CreateGroupFragment.CreateGroupFragListener {

    private val cm = MainApp.connManager

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var controller: ChatController

    private val onGroupJoinListener = Runnable {
        joinedGroup()
    }

    private val onGroupLeftListener = Runnable {
        leftGroup()
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


        InfoManager.grpManager.addJoinedGroupListener(onGroupJoinListener)
        InfoManager.grpManager.addLeftGroupListener(onGroupLeftListener)

        if (InfoManager.grpManager.inGroup) {
            joinedGroup()
        }
        else {
            leftGroup()
        }

    }

    override fun onDestroy() {
        InfoManager.grpManager.removeJoinedGroupListener(onGroupJoinListener)
        InfoManager.grpManager.removeLeftGroupListener(onGroupLeftListener)
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

        cm.stateManager.setToServer(gInfo)

        findNavController(R.id.nav_host_fragment).navigate(R.id.nav_chat)
    }


    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        currentFocus?.let {
            inputManager?.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }


    private fun joinedGroup() {
        runOnUiThread {
            controller.showNavDrawerItem(R.id.nav_chat)
            controller.showNavDrawerItem(R.id.nav_group_settings)
            controller.showNavDrawerItem(R.id.nav_members)
            controller.hideNavDrawerItem(R.id.nav_scan_network)

            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_chat)
        }
    }

    private fun leftGroup() {
        runOnUiThread {
            controller.showNavDrawerItem(R.id.nav_scan_network)
            controller.hideNavDrawerItem(R.id.nav_chat)
            controller.hideNavDrawerItem(R.id.nav_group_settings)
            controller.hideNavDrawerItem(R.id.nav_members)

            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_scan_network)
        }
    }

}
