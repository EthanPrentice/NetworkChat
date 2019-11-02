package com.ethanprentice.networkchat.activities.chat_activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.design.widget.NavigationView
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.chat_activity.ui.chat.ChatFragment
import com.ethanprentice.networkchat.activities.chat_activity.ui.scan_network.ScanNetworkFragment
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