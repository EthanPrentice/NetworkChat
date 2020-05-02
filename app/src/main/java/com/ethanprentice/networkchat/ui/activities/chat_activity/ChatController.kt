package com.ethanprentice.networkchat.ui.activities.chat_activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.frags.chat.ChatFragment
import com.ethanprentice.networkchat.ui.activities.chat_activity.frags.scan_network.ScanNetworkFragment
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.ChatMessage
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.ui.views.ChatMessageView


class ChatController(private val context: ChatActivity) {

    private val viewModel = ViewModelProviders.of(context).get(ChatViewModel::class.java)


    /**
     * Adds a DeviceInfoView to the LinearLayout using data included in the InfoResponse
     */
    fun addDeviceInfo(infoRsp: InfoResponse) {
        val navHost = context.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.fragments.let {frags ->
                for (frag in frags) {
                    if (frag is ScanNetworkFragment) {
                        context.runOnUiThread {
                            frag.addDeviceInfo(infoRsp)
                        }
                    }
                }
            }
        }
    }


    fun addChatMsgView(chatBroadcast: ChatBroadcast) {
        val view = ChatMessageView(context, chatBroadcast.ip, chatBroadcast.chatMsg, chatBroadcast.sender)
        addChatMsgView(view)
    }

    fun addChatMsgView(chatMsg: ChatMessage) {
        val view = ChatMessageView(context, chatMsg.ip, chatMsg.chatText, chatMsg.sender)
        addChatMsgView(view)
    }

    private fun addChatMsgView(view: ChatMessageView) {
        viewModel.chatMsgViews.observe(context, Observer {
            it?.add(view)
        })
        val navHost = context.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.fragments.let {frags ->
                for (frag in frags) {
                    if (frag is ChatFragment) {
                        frag.addChatMsgView(view)
                    }
                }
            }
        }
    }

    fun hideNavDrawerItem(itemId: Int) {
        val navigationView = context.findViewById<NavigationView>(R.id.nav_view)
        context.runOnUiThread {
            navigationView.menu.findItem(itemId).isVisible = false
        }
    }

    fun showNavDrawerItem(itemId: Int) {
        val navigationView = context.findViewById<NavigationView>(R.id.nav_view)
        context.runOnUiThread {
            navigationView.menu.findItem(itemId).isVisible = true
        }
    }

}