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
import com.ethanprentice.networkchat.connection_manager.service.SocketListenerService
import com.ethanprentice.networkchat.message_router.MessageRouter
import com.ethanprentice.networkchat.tasks.SendUdpMessage
import java.net.InetAddress

/**
 * Handles messages that are redirected to connManager by MessageRouter
 *
 * @author Ethan Prentice
 */
class CmMessageHandler(private val cm: ConnectionManager, msgRouter: MessageRouter) : MessageHandler(msgRouter) {

    override val handlerName = "connection-manager"

    /**
     * Registers all [cm] related endpoints with the [MessageRouter] so [Message]s can be routed here
     */
    override fun register() {
        msgRouter.endpointManager.registerHandler(this)
        msgRouter.endpointManager.registerEndpoint(CmConfig.CONN_REQ_ENDPOINT)
        msgRouter.endpointManager.registerEndpoint(CmConfig.CONN_RSP_ENDPOINT)
        msgRouter.endpointManager.registerEndpoint(CmConfig.INFO_REQ_ENDPOINT)
        msgRouter.endpointManager.registerEndpoint(CmConfig.SEND_CONN_REQ_EP)
        msgRouter.endpointManager.registerEndpoint(CmConfig.CHAT_MESSAGE)
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
        val address = InfoManager.getDeviceIp().hostAddress
        val port = SocketListenerService.getUdpPort()

        if (port == null) {
            Log.e(TAG, "Could not send the InfoResponse, SocketListenerService must be running!")
            return
        }

        val deviceName = android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL
        val displayName = InfoManager.userInfo.displayName
        val groupName = InfoManager.groupInfo?.groupName

        val message = InfoResponse(address, port, groupName ?: displayName ?: deviceName, "placeholder_url.png")

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
            val message = ConnectionRequest(InfoManager.getDeviceIp().hostAddress, it, ConnType.CLIENT.name)
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

        // TODO: Authenticate / prompt user to accept the connection request instead of automatically accepting if in server mode
        val address = InfoManager.getDeviceIp().hostAddress
        val port = SocketListenerService.getUdpPort()

        if (port == null) {
            Log.e(TAG, "Could not send the ConnectionResponse, SocketListenerService must be running!")
            return
        }

        val message = if (cm.isServer()) {
            val socket: ShakaServerSocket = cm.openTcpSocket()
            socket.accept()
            ConnectionResponse(address, port, true, socket.localPort, InfoManager.groupInfo)
        }
        else {
            Log.v(TAG, "A device tried to connect when the device is not acting as a server. (Request discarded)")
            ConnectionResponse(address, port, false, null, InfoManager.groupInfo)
        }

        SendUdpMessage(InetAddress.getByName(connReq.ip), connReq.port, message).execute()
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
            }
        }
        else {
            // TODO: Show user connection rejection with a ui prompt
        }
    }

    /**
     * Should only be called when the device is acting as a server
     * This will add the message to this devices UI, and broadcast the message to it's connected devices
     */
    private fun handleChatMsg(chatMsg: ChatMessage) {
        val broadcast = ChatBroadcast(chatMsg.ip, chatMsg.chatText, chatMsg.sender)
        cm.writeToTcp(broadcast)

        MainApp.currActivity?.let {
            if (it is ChatActivity) {
                it.controller.addChatMsgView(chatMsg)
            }
        }
    }


    companion object {
        private val TAG = this::class.java.canonicalName
    }

}