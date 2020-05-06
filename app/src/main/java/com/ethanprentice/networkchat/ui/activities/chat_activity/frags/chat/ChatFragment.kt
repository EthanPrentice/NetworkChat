package com.ethanprentice.networkchat.ui.activities.chat_activity.frags.chat

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatViewModel
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
                val userInfo = InfoManager.userInfo
                userInfo.changeBmpResolution(60, 60)
                val msg = ChatMessage(InfoManager.deviceIp.hostAddress, text, userInfo)
                MessageRouter.handleMessage(msg)
            }
            activity?.runOnUiThread {
                chat_msg_input?.text?.clear()
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