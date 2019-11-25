package com.ethanprentice.networkchat.ui.activities.chat_activity

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.ethanprentice.networkchat.ui.views.ChatMessageView

class ChatViewModel : ViewModel() {

    val chatMsgViews = MutableLiveData<ArrayList<ChatMessageView>>().apply {
        value = ArrayList()
    }

}