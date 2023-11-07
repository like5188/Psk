package com.psk.socket

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Socket连接的服务
 */
class SocketService : Service() {
    override fun onCreate() {
        super.onCreate()
        Log.i("SocketService", "onCreate")
        SocketServer.socketListener = object : SocketListener {
            override fun onConnected() {
                Log.i("SocketService", "onConnected")
            }

            override fun onDisConnected() {
                Log.i("SocketService", "onDisConnected")
            }

            override fun onReceived(msg: String?) {
                Log.i("SocketService", "onReceived $msg")
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("SocketService", "onStartCommand")
        // 连接socket服务器
        SocketServer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketServer.stop()
        Log.i("SocketService", "onDestroy")
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("SocketService", "onBind")
        return null
    }

    companion object {

        /**
         * 启动 Socket 服务并启动 socketserver
         */
        fun start(context: Context) {
            Log.i("SocketService", "start")
            val intent = Intent(context, SocketService::class.java)
            Log.i("SocketService", "startService=" + context.startService(intent))
        }

        /**
         * 销毁socket服务
         */
        fun stop(context: Context) {
            Log.i("SocketService", "stop")
            val intent = Intent(context, SocketService::class.java)
            context.stopService(intent)
        }
    }
}