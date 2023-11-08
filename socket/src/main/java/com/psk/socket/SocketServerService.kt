package com.psk.socket

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import java.net.InetSocketAddress

/**
 * Socket server 服务
 */
class SocketServerService : IntentService("SocketServerService") {
    private lateinit var server: SocketServer

    override fun onCreate() {
        super.onCreate()
        Log.i("SocketServerService", "onCreate")
    }

    override fun onHandleIntent(intent: Intent?) {
        intent ?: return
        if (!::server.isInitialized) {
            val port = intent.getIntExtra(KEY_SOCKET_PORT, -1)
            if (port != -1) {
                server = SocketServer(InetSocketAddress(port))
                try {
                    server.run()// 阻塞
                } catch (e: Exception) {
                    Log.e("SocketServerService", e.message ?: "")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SocketServerService", "onDestroy")
        server.stop()
    }

    companion object {
        private const val KEY_SOCKET_PORT = "socket_port"

        /**
         * 启动 Socket 服务并启动 socketserver
         */
        fun start(context: Context, port: Int) {
            Log.i("SocketServerService", "start")
            val intent = Intent(context, SocketServerService::class.java)
            intent.putExtra(KEY_SOCKET_PORT, port)
            Log.i("SocketServerService", "startService=" + context.startService(intent))
        }

        /**
         * 销毁socket服务
         */
        fun stop(context: Context) {
            Log.i("SocketServerService", "stop")
            val intent = Intent(context, SocketServerService::class.java)
            context.stopService(intent)
        }
    }
}