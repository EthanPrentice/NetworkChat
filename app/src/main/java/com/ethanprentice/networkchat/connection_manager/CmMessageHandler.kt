package com.ethanprentice.networkchat.connection_manager

import android.util.Log
import com.ethanprentice.networkchat.MainApp
import com.ethanprentice.networkchat.activities.chat_activity.ChatActivity
import com.ethanprentice.networkchat.adt.Message
import com.ethanprentice.networkchat.adt.ShakaServerSocket
import com.ethanprentice.networkchat.adt.enums.ConnType
import com.ethanprentice.networkchat.connection_manager.messages.*
import com.ethanprentice.networkchat.information_manager.InfoManager
import com.ethanprentice.networkchat.adt.MessageHandler
import com.ethanprentice.networkchat.message_router.MessageRouter
import com.ethanprentice.networkchat.tasks.SendUdpMessage
import java.net.InetAddress

/**
 * Handles messages that are redirected to ConnectionManager by MessageRouter
 *
 * @author Ethan Prentice
 */
class CmMessageHandler(msgRouter: MessageRouter) : MessageHandler(msgRouter) {

    override val handlerName = "connection-manager"

    /**
     * Registers all [ConnectionManager] related endpoints with the [MessageRouter] so [Message]s can be routed here
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
        val address = ConnectionManager.getDeviceIp().hostAddress
        val port = UdpListenerService.port

        if (port == null) {
            Log.e(TAG, "Could not send the InfoResponse, UdpListenerService must be running!")
            return
        }

        if (ConnectionManager.isServer) {
            val deviceName = android.os.Build.MANUFACTURER + " - " + android.os.Build.MODEL
            val displayName = InfoManager.userInfo.displayName
            val groupName = InfoManager.groupInfo?.groupName

            val message = InfoResponse(address, port, groupName ?: displayName ?: deviceName, "placeholder_url.png")
            SendUdpMessage(InetAddress.getByName(infoReq.ip), infoReq.port, message).execute()
        }
        else {
            Log.v(TAG, "A device tried to connect when the device is not acting as a server. (Request discarded)")
        }

    }

    /**
     * Receives a ConnectionRequest internally to be redirected externally by ConnectionManager
     */
    private fun sendConnReq(connReq: ConnectionRequest) {
        val targetAddr = connReq.ip
        val targetPort = connReq.port

        val message = ConnectionRequest(ConnectionManager.getDeviceIp().hostAddress, UdpListenerService.port!!, ConnType.CLIENT.name)

        // Device is requesting to connect to the server, so it is not a server
        ConnectionManager.isServer = false

        SendUdpMessage(InetAddress.getByName(targetAddr), targetPort, message).execute()
    }

    /**
     * Receives a ConnectionRequest externally and sends back a ConnectionResponse
     * Later we need to implement a user prompt so connection is accepted by both parties
     */
    private fun handleConnReq(connReq: ConnectionRequest) {

        if (!ConnectionManager.isServer) {
            Log.v(TAG, "A device tried to connect when the device is not acting as a server. (Request discarded)")
            return
        }

        // TODO: Authenticate / prompt user to accept the connection request instead of automatically accepting if in server mode
        val address = ConnectionManager.getDeviceIp().hostAddress
        val port = UdpListenerService.port

        if (port == null) {
            Log.e(TAG, "Could not send the ConnectionResponse, UdpListenerService must be running!")
            return
        }

        val message = if (ConnectionManager.isServer) {
            val socket: ShakaServerSocket = ConnectionManager.openTcpSocket()
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
        val port = UdpListenerService.port

        if (port == null) {
            Log.e(TAG, "Could not send the ConnectionResponse, UdpListenerService must be running!")
            return
        }

        if (connRsp.accepted) {
            if (connRsp.tcpPort == null) {
                Log.e(TAG, "ConnectionResponse is invalid.  Cannot have a null tcpPort when accepted is true.")
            }
            else {
                // TODO: we should create a state manager to manage the CM before it becomes more advanced.  Then change this to use the state manager
                ConnectionManager.isServer = false
                ConnectionManager.openClientSocket(connRsp.ip, connRsp.tcpPort)
                InfoManager.groupInfo = connRsp.groupInfo
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
        MainApp.currActivity?.let {
            if (it is ChatActivity) {
                it.controller.addChatMsgView(chatMsg)
            }
        }

        val broadcast = ChatBroadcast(chatMsg.ip, chatMsg.chatText, chatMsg.sender)
        ConnectionManager.writeToTcp(broadcast)
    }


    companion object {
        private val TAG = this::class.java.canonicalName
    }

}