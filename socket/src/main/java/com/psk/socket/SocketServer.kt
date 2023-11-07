package com.psk.socket

import org.smartboot.socket.extension.protocol.StringProtocol
import org.smartboot.socket.transport.AioQuickServer

object SocketServer {
    var socketListener: SocketListener? = null
    private var server: AioQuickServer? = null

    @Synchronized
    fun start(port: Int) {
        val listener: SocketListener = object : SocketListener {
            override fun onConnected() {
                socketListener?.onConnected()
            }

            override fun onDisConnected() {
                socketListener?.onDisConnected()
            }

            override fun onReceived(msg: String?) {
                socketListener?.onReceived(msg)
            }
        }
        server = AioQuickServer(port, StringProtocol(), SocketMessageProcessor(listener))
        server?.start()
    }

    fun stop() {

    }
}