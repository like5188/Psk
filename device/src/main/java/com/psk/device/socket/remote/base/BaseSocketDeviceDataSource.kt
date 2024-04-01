package com.psk.device.socket.remote.base

import android.annotation.SuppressLint
import com.like.common.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

abstract class BaseSocketDeviceDataSource {
    private lateinit var webSocketServer: WebSocketServer

    /**
     * 连接成功
     */
    private var onOpen: ((address: String?) -> Unit)? = null

    /**
     * 连接关闭
     */
    private var onClose: ((code: Int, reason: String?) -> Unit)? = null

    /**
     * 服务器发生错误
     */
    private var onError: ((e: Exception?) -> Unit)? = null

    /**
     * 接收到消息
     */
    private var onMessage: ((message: ByteBuffer?) -> Unit)? = null

    fun init(hostName: String?, port: Int) {
        val address = if (hostName.isNullOrEmpty()) {
            InetSocketAddress(port)
        } else {
            InetSocketAddress(hostName, port)
        }
        webSocketServer = object : WebSocketServer(address) {
            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                onOpen?.invoke(conn?.remoteSocketAddress?.address?.hostAddress)
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                onClose?.invoke(code, reason)
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
            }

            override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
                super.onMessage(conn, message)
                onMessage?.invoke(message)
            }

            override fun onError(conn: WebSocket?, ex: java.lang.Exception?) {
                onError?.invoke(ex)
            }

            override fun onStart() {
            }
        }

        // onClose code=1006 reason=The connection was closed because the other endpoint did not respond with a pong in time.
        webSocketServer.connectionLostTimeout = 0

        // 解决异常：java.net.BindException: Address already in use
        webSocketServer.isReuseAddr = true
    }

    fun isConnected(): Boolean {
        return webSocketServer.connections.firstOrNull()?.isOpen == true
    }

    @SuppressLint("MissingPermission")
    fun connect(
        scope: CoroutineScope,
        onOpen: ((address: String?) -> Unit)? = null,
        onClose: ((code: Int, reason: String?) -> Unit)? = null,
        onError: ((e: Exception?) -> Unit)? = null,
    ) {
        this.onOpen = onOpen
        this.onClose = onClose
        this.onError = onError
        scope.launch(Dispatchers.IO) {
            try {
                webSocketServer.run()// 阻塞
            } catch (e: Exception) {
                Logger.e("connect 失败：${e.message}")
                onError?.invoke(e)
            }
        }
    }

    fun setOnMessageCallback(onMessage: (message: ByteBuffer?) -> Unit) {
        this.onMessage = onMessage
    }

    fun close() {
        try {
            webSocketServer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}