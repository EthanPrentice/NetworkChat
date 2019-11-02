package com.ethanprentice.networkchat.activities.chat_activity.ui.scan_network

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.ethanprentice.networkchat.MainApp

import com.ethanprentice.networkchat.R
import com.ethanprentice.networkchat.activities.MainActivity
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.adt.SerializableMessage
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.ConnectionManager
import com.ethanprentice.networkchat.connection_manager.messages.ChatMessage
import com.ethanprentice.networkchat.connection_manager.messages.InfoResponse
import com.ethanprentice.networkchat.ui.frags.CreateGroupFragment
import com.ethanprentice.networkchat.ui.views.DeviceInfoView
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_scan_network.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * Sends a broadcast over the network to find other devices running Shaka and displays them to the user
 *
 * @author Ethan Prentice
 */
class ScanNetworkFragment : Fragment(), CreateGroupFragment.CreateGroupFragListener {
    private var listener: ScanNetworkFragListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (container == null) {
            null
        }
        else {
            inflater.inflate(R.layout.fragment_scan_network, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setScanOnClick()
        setSendTestOnClick()
        setCreateGroupOnClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ScanNetworkFragListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ScanNetworkFragListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onCreateGroup(frag: CreateGroupFragment, gInfo: GroupInfo) {
        val activity = activity
        if (activity is ChatActivity) {
            activity.onCreateGroup(frag, gInfo)
        }
    }

    fun addDeviceInfo(infoRsp: InfoResponse) {
        try {
            val deviceInfo = DeviceInfoView(activity!!, infoRsp)

            found_devices_ll?.addView(deviceInfo)
        }
        catch(e: Exception) {
            Log.w(TAG, "Could not add device info to ui fragment", e)
        }
    }

    private fun setScanOnClick() {
        test_scan_btn?.setOnClickListener {
            ConnectionManager.scanNetwork()
            found_devices_ll.removeAllViews()
        }
    }

    private fun setSendTestOnClick() {
        @Serializable
        class TestMsg(val msg: String) : SerializableMessage() {
            override fun toJsonString(): String {
                return Json.stringify(serializer(), this)
            }
            override var endpointName = "none"
        }

        send_test_tcp_btn?.setOnClickListener {
            ConnectionManager.writeToTcp(TestMsg("This is a test!"))
        }

    }

    private fun setCreateGroupOnClick() {
        create_group_btn?.setOnClickListener {
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.add(R.id.scan_network_frag_container, CreateGroupFragment.newInstance())
                    ?.commit()
        }
    }


    interface ScanNetworkFragListener

    companion object {
        private val TAG = this::class.java.canonicalName

        @JvmStatic
        fun newInstance() = ScanNetworkFragment()
    }
}
