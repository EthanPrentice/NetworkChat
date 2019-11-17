package com.ethanprentice.networkchat.activities.chat_activity.ui.chat

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.activities.chat_activity.ChatViewModel
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.messages.ChatBroadcast
import com.ethanprentice.networkchat.connection_manager.messages.ChatMessage
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import com.ethanprentice.networkchat.ui.views.ChatMessageView
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlin.concurrent.thread

class ChatFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chat_msg_send_btn.setOnClickListener {
            val text = chat_msg_input.text.toString()
            // send text in input field to the message handler to be sent to other devices
            // needs to be in separate thread since we are accessing device ip
            thread(start=true) {
                val msg = ChatMessage(InfoManager.getDeviceIp().canonicalHostName, text, InfoManager.userInfo)
                MessageRouter.handleMessage(msg)
            }
            activity?.runOnUiThread {
                chat_msg_input?.text?.clear()
            }

            // hide keyboard
            activity?.let {
                if (it is ChatActivity) {
                    it.hideKeyboard()
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity?.let {
            val sharedViewModel = ViewModelProviders.of(it).get(ChatViewModel::class.java)

            sharedViewModel.chatMsgViews.observe(this, Observer {views ->
                views?.forEach {view ->
                    (view.parent as? LinearLayout)?.removeView(view)
                    addChatMsgView(view)
                }

            })
        }
    }

    fun addChatMsgView(view: ChatMessageView) {
        activity?.runOnUiThread {
            chat_msg_ll.addView(view)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = ChatFragment()
    }
}