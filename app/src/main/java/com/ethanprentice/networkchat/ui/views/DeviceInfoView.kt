package com.ethanprentice.networkchat.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.adt.enums.ConnType
import com.ethanprentice.networkchat.connection_manager.CmConfig
import com.ethanprentice.networkchat.connection_manager.messages.ConnectionRequest
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import com.makeramen.roundedimageview.RoundedImageView

/**
 * @author Ethan Prentice
 *
 * Displays device info that is received from an InfoResponse after scanning the network
 * @param context Activity context
 * @param infoRsp The response received by CM to be displayed
 */
class DeviceInfoView(context: Context, private val infoRsp: InfoResponse) : FrameLayout(context) {

    // TODO: once we implement Spotify capabilities use this imageView to display the user's profile picture
    private val layout: RelativeLayout

    init {
        setLayoutParams()

        val inflater = LayoutInflater.from(context)
        layout = inflater.inflate(R.layout.view_device_info, null) as RelativeLayout

        val groupNameTextView = layout.findViewById<TextView>(R.id.device_info_group_name)
        val groupOwnerTextView = layout.findViewById<TextView>(R.id.device_info_group_owner)
        val groupImgView = layout.findViewById<RoundedImageView>(R.id.device_info_img)

        groupNameTextView.text = infoRsp.groupName
        groupOwnerTextView.text = infoRsp.ownerName
        // groupImgView.setImageBitmap(infoRsp.ownerImg)

        layout.setOnClickListener {
            // TODO: Change this
            // Connection type doesn't matter here since we're only sending it internally to CM to be resent to the target device
            // CM will decide the connection type
            val uDispName = InfoManager.userInfo.displayName
            val connReq = ConnectionRequest(infoRsp.ip, infoRsp.port, ConnType.CLIENT.name, uDispName)
            connReq.endpointName = CmConfig.SEND_CONN_REQ_EP.name

            MessageRouter.handleMessage(connReq)

            Toast.makeText(context, "Requested to join %s!".format(infoRsp.groupName), Toast.LENGTH_SHORT).show()
        }

        // add subviews to view
        addView(layout)
    }

    private fun setLayoutParams() {
        layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }
}