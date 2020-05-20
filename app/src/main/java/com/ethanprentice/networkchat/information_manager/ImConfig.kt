package com.ethanprentice.networkchat.information_manager

import com.ethanprentice.networkchat.adt.Endpoint
import com.ethanprentice.networkchat.information_manager.messages.UserInfoMessage

object ImConfig {

    val UINFO_UPDATE_ENDPOINT = Endpoint("com.ethanprentice.shaka/information-manager/update-user-info", UserInfoMessage::class)

}