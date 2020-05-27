package com.ethanprentice.networkchat.connection_manager

import android.util.Log
import androidx.navigation.findNavController
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.ui.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.enums.ConnType
import com.ethanprentice.networkchat.connection_manager.messages.*
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.adt.UserInfo
import com.ethanprentice.networkchat.connection_manager.service.SocketListenerService
import com.ethanprentice.networkchat.information_manager.messages.UserInfoMessage
import com.ethanprentice.networkchat.message_router.EndpointManager
import com.ethanprentice.networkchat.message_router.MessageRouter
import com.ethanprentice.networkchat.tasks.SendUdpMessage
import com.ethanprentice.networkchat.ui.frags.ConnRequestFragment
import java.net.InetAddress
import kotlin.concurrent.thread

/**
 * Handles messages that are redirected to connManager by MessageRouter
 *
 * @author Ethan Prentice
 */
class CmMessageHandler(private val cm: ConnectionManager, endpointManager: EndpointManager) : MessageHandler(endpointManager) {

    override val handlerName = "connection-manager"

    /**
     * Registers all [cm] related endpoints with the [MessageRouter] so [Message]s can be routed here
     */
    override fun register() {
        endpointManager.registerHandler(this)
        endpointManager.registerEndpoint(CmConfig.CONN_REQ_ENDPOINT)
        endpointManager.registerEndpoint(CmConfig.CONN_RSP_ENDPOINT)
        endpointManager.registerEndpoint(CmConfig.INFO_REQ_ENDPOINT)
        endpointManager.registerEndpoint(CmConfig.SEND_CONN_REQ_EP)
        endpointManager.registerEndpoint(CmConfig.CHAT_MESSAGE)
    }


    override fun handleMessage(_message: Message) {
        when (_message.endpointName) {
            CmConfig.INFO_REQ_ENDPOINT.name -> handleInfoReq(_message as InfoRequest)
            CmConfig.CONN_REQ_ENDPOINT.name -> handleConnReq(_message as ConnectionRequest)
            CmConfig.CONN_RSP_ENDPOINT.name -> handleConnRsp(_message as ConnectionResponse)
            CmConfig.SEND_CONN_REQ_EP.name  -> sendConnReq(_message as ConnectionRequest)
            CmConfig.CHAT_MESSAGE.name      -> handleChatMsg(_message as ChatMessage)
        }
    }

    /**
     * Sends an InfoResponse to let the requester know that this device is also running Shaka on the same network
     */
    private fun handleInfoReq(infoReq: InfoRequest) {
        val address = InfoManager.deviceIp.hostAddress
        val port = SocketListenerService.getUdpPort()

        if (port == null) {
            Log.e(TAG, "Could not send the InfoResponse, SocketListenerService must be running!")
            return
        }

        val deviceName = android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL
        val displayName = InfoManager.userInfo.displayName
        val groupName = InfoManager.groupInfo?.groupName
        val groupImg = InfoManager.userInfo.getImageBmp(MainApp.context)

        val message = InfoResponse(address, port, groupName ?: deviceName, displayName)

        if (cm.isClient()) {
            cm.writeToTcp(message)
        }
        else if (cm.isServer()) {
            SendUdpMessage(InetAddress.getByName(infoReq.ip), infoReq.port, message).execute()
        }

    }

    /**
     * Receives a ConnectionRequest internally to be redirected externally by connManager
     */
    private fun sendConnReq(connReq: ConnectionRequest) {
        val targetAddr = connReq.ip
        val targetPort = connReq.port

        SocketListenerService.getUdpPort()?.let {
            val uDispName = InfoManager.userInfo.displayName
            val message = ConnectionRequest(InfoManager.deviceIp.hostAddress, it, ConnType.CLIENT.name, uDispName)
            SendUdpMessage(InetAddress.getByName(targetAddr), targetPort, message).execute()
        }
    }

    /**
     * Receives a ConnectionRequest externally and sends back a ConnectionResponse
     * Later we need to implement a user prompt so connection is accepted by both parties
     */
    private fun handleConnReq(connReq: ConnectionRequest) {

        if (!cm.isServer()) {
            Log.v(TAG, "A device tried to connect when the device is not acting as a server. (Request discarded)")
            return
        }

        val port = SocketListenerService.getUdpPort()
        if (port == null) {
            Log.e(TAG, "Could not send the ConnectionResponse, SocketListenerService must be running!")
            return
        }

        MainApp.currActivity?.let {
            if (it is ChatActivity) {
                val newFragment = ConnRequestFragment.newInstance(connReq.userDispName, connReq.ip, connReq.port)
                newFragment.show(it.supportFragmentManager, "conn_req_dialog")
            }
        }
    }


    /**
     *
     */
    private fun handleConnRsp(connRsp: ConnectionResponse) {
        val port = SocketListenerService.getUdpPort()

        if (port == null) {
            Log.e(TAG, "Could not send the ConnectionResponse, SocketListenerService must be running!")
            return
        }

        if (connRsp.accepted) {
            if (connRsp.tcpPort == null) {
                Log.e(TAG, "ConnectionResponse is invalid.  Cannot have a null tcpPort when accepted is true.")
            }
            else if (connRsp.groupInfo == null) {
                Log.e(TAG, "ConnectionResponse is invalid.  Cannot have a null groupInfo when accepted is true.")
            }
            else {
                cm.stateManager.setToClient(connRsp.ip, connRsp.tcpPort, connRsp.groupInfo)

                //thread(start=true) {
                    // Send the server device a message with this devices user information
                    val uInfoMessage = UserInfoMessage(InfoManager.deviceIp.hostAddress, InfoManager.userInfo)
                    cm.writeToTcp(uInfoMessage)
                //}
            }
        }
        else {
            // TODO: Show user connection rejection with a ui prompt
        }
    }

    /**
     * Forwards the messages to other devices if the server, or to the server for rebroadcasting if a client
     * If the device is also a server, it will add the message to the UI
     */
    private fun handleChatMsg(chatMsg: ChatMessage) {
        val broadcast = if (cm.isServer()) {
            MainApp.currActivity?.let {
                if (it is ChatActivity) {
                    it.runOnUiThread {
                        it.controller.addChatMsgView(chatMsg)
                    }
                }
            }

            ChatBroadcast(chatMsg.ip, chatMsg.chatText, chatMsg.sender)
        }
        else {
            ChatMessage(chatMsg.ip, chatMsg.chatText, chatMsg.sender)
        }

        cm.writeToTcp(broadcast)
    }


    companion object {
        private val TAG = this::class.java.canonicalName
    }

}