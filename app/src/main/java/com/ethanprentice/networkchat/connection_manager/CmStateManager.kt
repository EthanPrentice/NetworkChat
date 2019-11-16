package com.ethanprentice.networkchat.connection_manager

import com.ethanprentice.networkchat.adt.GroupInfo
import com.ethanprentice.networkchat.information_manager.InfoManager

class CmStateManager(private val cm: ConnectionManager) {

    var currentState = ConnectionState.UNCONNECTED
        private set


    /**
     * Server state we should have only one port open, a TCP connection to the server
     */
    fun setToClient(ip: String, port: Int, gInfo: GroupInfo) {
        if (currentState == ConnectionState.CLIENT) {
            return
        }

        val prevState = currentState
        currentState = ConnectionState.CLIENT

        cm.openClientSocket(ip, port)
        InfoManager.groupInfo = gInfo

        if (prevState == ConnectionState.UNCONNECTED) {
            cm.closeUdpSocket()
        }
        else if (prevState == ConnectionState.SERVER) {
            cm.closeUdpSocket()
            cm.closeServerSockets()
        }

    }


    /**
     * Unconnected state we should have UDP port open, and no TCP ports open
     */
    fun setToUnconnected() {
        if (currentState == ConnectionState.UNCONNECTED) {
            return
        }

        val prevState = currentState
        currentState = ConnectionState.UNCONNECTED

        if (prevState == ConnectionState.CLIENT) {
            cm.openUdpSocket()
            cm.closeClientSocket()
        }
        else {
            cm.closeServerSockets()
        }

        InfoManager.groupInfo = null
    }


    /**
     * Server state we should have UDP port open and add TCP server sockets as clients join
     */
    fun setToServer(gInfo: GroupInfo) {
        if (currentState == ConnectionState.SERVER) {
            return
        }

        val prevState = currentState
        currentState = ConnectionState.SERVER

        if (prevState == ConnectionState.UNCONNECTED) {

        }
        else if (prevState == ConnectionState.CLIENT) {
            cm.openUdpSocket()
            cm.closeClientSocket()
        }

        InfoManager.groupInfo = gInfo
    }

}