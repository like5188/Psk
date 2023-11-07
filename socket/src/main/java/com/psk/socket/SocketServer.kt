package com.psk.socket

import android.util.Log
import org.smartboot.socket.extension.protocol.StringProtocol
import org.smartboot.socket.transport.AioQuickServer
import java.net.InetAddress

object SocketServer {
    var socketListener: SocketListener? = null
    private var server: AioQuickServer? = null

    @Synchronized
    fun start() {
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
        val address = InetAddress.getLocalHost()
        Log.i("SocketServer", "start server ${address.hostName} ${address.hostAddress} 8888")
        server = AioQuickServer(8888, StringProtocol(), SocketMessageProcessor(listener))
        server?.start()
    }

    fun stop() {

    }
}