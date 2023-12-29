package com.psk.socket

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * Socket server 服务
 */
class SocketServerService : IntentService(TAG) {
    private lateinit var server: SocketServer

    @Deprecated("Deprecated in Java")
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        intent ?: return
        if (!::server.isInitialized) {
            val port = intent.getIntExtra(KEY_SOCKET_PORT, -1)
            if (port != -1) {
                server = SocketServer(InetSocketAddress(port), listener)
                server.connectionLostTimeout =
                    0// onClose code=1006 reason=The connection was closed because the other endpoint did not respond with a pong in time.
                server.isReuseAddr = true// 解决异常：java.net.BindException: Address already in use
                try {
                    server.run()// 阻塞
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "")
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        server.stop()
    }

    companion object {
        private const val KEY_SOCKET_PORT = "socket_port"
        private val TAG = SocketServerService::class.java.simpleName
        private var listener: SocketListener? = null

        /**
         * 启动 Socket 服务并启动 socketserver
         */
        fun start(context: Context, port: Int, listener: SocketListener? = null) {
            Log.i(TAG, "start")
            SocketServerService.listener = listener
            val intent = Intent(context, SocketServerService::class.java)
            intent.putExtra(KEY_SOCKET_PORT, port)
            Log.i(TAG, "startService=" + context.startService(intent))
        }

        /**
         * 销毁socket服务
         */
        fun stop(context: Context) {
            Log.i(TAG, "stop")
            val intent = Intent(context, SocketServerService::class.java)
            context.stopService(intent)
        }
    }
}

internal class SocketServer(address: InetSocketAddress, private val listener: SocketListener? = null) : WebSocketServer(address) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake) {
        conn?.send("Welcome to the server!") //This method sends a message to the new client
        broadcast("new connection: " + handshake.resourceDescriptor) //This method sends a message to all clients connected
        println("new connection to " + conn?.remoteSocketAddress)
        listener?.onOpen(conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String, remote: Boolean) {
        println("closed " + conn?.remoteSocketAddress + " with exit code " + code + " additional info: " + reason)
        listener?.onClose(code, reason)
    }

    override fun onMessage(conn: WebSocket?, message: String) {
        println("received message from " + conn?.remoteSocketAddress + ": " + message)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        println("received ByteBuffer from " + conn?.remoteSocketAddress)
        message ?: return
        listener?.onMessage(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        System.err.println("an error occurred on connection " + conn?.remoteSocketAddress + ":" + ex)
        listener?.onError(ex)
    }

    override fun onStart() {
        println("server started successfully")
    }

}

interface SocketListener {
    /**
     * 连接成功
     */
    fun onOpen(address: String?)

    /**
     * 连接关闭
     */
    fun onClose(code: Int, reason: String)

    /**
     * 服务器发生错误
     */
    fun onError(e: Exception)

    /**
     * 接收到消息
     */
    fun onMessage(message: ByteBuffer)
}
