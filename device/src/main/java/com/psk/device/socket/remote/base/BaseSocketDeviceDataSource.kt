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
     * 启动服务成功
     */
    private var onStart: (() -> Unit)? = null

    /**
     * 客户端连接本服务成功
     */
    private var onOpen: ((address: String?) -> Unit)? = null

    /**
     * 客户端连接本服务关闭
     */
    private var onClose: ((code: Int, reason: String?) -> Unit)? = null

    /**
     * 发生错误
     */
    private var onError: ((e: Exception?) -> Unit)? = null

    /**
     * 接收到客户端发来的消息
     */
    private var onMessage: ((message: ByteBuffer?) -> Unit)? = null

    /**
     * @param hostName  本服务器开放的地址。默认为 null，表示需要连接的设备与本服务器处于局域网。
     * @param port      本服务器开放的端口号。默认为 7777。
     */
    fun init(hostName: String?, port: Int) {
        val address = if (hostName.isNullOrEmpty()) {
            InetSocketAddress(port)
        } else {
            InetSocketAddress(hostName, port)
        }
        webSocketServer = object : WebSocketServer(address) {
            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                onOpen?.invoke(conn?.remoteSocketAddress?.address?.hostAddress)
                Logger.d("onOpen")
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                onClose?.invoke(code, reason)
                Logger.d("onClose")
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
            }

            override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
                super.onMessage(conn, message)
                Logger.d("onMessage")
                onMessage?.invoke(message)
            }

            override fun onError(conn: WebSocket?, ex: java.lang.Exception?) {
                Logger.d("onError")
                onError?.invoke(ex)
            }

            override fun onStart() {
                Logger.d("onStart")
                onStart?.invoke()
            }
        }

        // onClose code=1006 reason=The connection was closed because the other endpoint did not respond with a pong in time.
        webSocketServer.connectionLostTimeout = 0

        // 解决异常：java.net.BindException: Address already in use
        webSocketServer.isReuseAddr = true
    }

    fun isOpen(): Boolean {
        return webSocketServer.connections.firstOrNull()?.isOpen == true
    }

    @SuppressLint("MissingPermission")
    fun run(
        scope: CoroutineScope,
        onStart: (() -> Unit)? = null,
        onOpen: ((address: String?) -> Unit)? = null,
        onClose: ((code: Int, reason: String?) -> Unit)? = null,
        onError: ((e: Exception?) -> Unit)? = null,
    ) {
        this.onStart = onStart
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

    fun stop() {
        try {
            webSocketServer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}