package com.psk.socket

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.InetSocketAddress
import kotlin.concurrent.thread

/**
 * Socket server 服务
 */
class SocketServerService : Service() {
    private lateinit var server: SocketServer

    override fun onCreate() {
        super.onCreate()
        Log.i("SocketServerService", "onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("SocketServerService", "onStartCommand")
        if (!::server.isInitialized) {
            val port = intent.getIntExtra(KEY_SOCKET_PORT, -1)
            server = SocketServer(InetSocketAddress(port))
        }
        // 连接socket服务器
        thread {
            try {
                server.run()
            } catch (e: Exception) {
                Log.e("SocketServerService", e.message ?: "")
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SocketServerService", "onDestroy")
        server.stop()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("SocketServerService", "onBind")
        return null
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