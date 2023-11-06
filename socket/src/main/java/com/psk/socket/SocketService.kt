package com.psk.socket

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson

/**
 * Socket连接的服务
 */
class SocketService : Service() {
    private val gson: Gson = Gson()
    override fun onCreate() {
        super.onCreate()
        Log.i("SocketService", "onCreate")
        LiveEventBusUtils.getInstance().setSendDataToServerCallback(SocketClient::sendMsg)
        SocketClient.socketListener = object : SocketListener {
            override fun onConnected() {
                LiveEventBusUtils.sendSocketConnectStateToH5(1)
            }

            override fun onDisConnected() {
                LiveEventBusUtils.sendSocketConnectStateToH5(0)
            }

            override fun onReceived(msg: String?) {
                // 转发H5
                LiveEventBusUtils.sendServerDataToH5(gson.fromJson(msg, Msg::class.java))
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("SocketService", "onStartCommand")
        val host = intent.getStringExtra(KEY_SOCKET_HOST)
        val port = intent.getIntExtra(KEY_SOCKET_PORT, -1)
        val connectTimeoutMillis = intent.getIntExtra(KEY_SOCKET_CONNECT_TIMEOUT_MILLIS, -1)
        val reconnectIntervalMillis = intent.getLongExtra(KEY_SOCKET_RECONNECT_INTERVAL_MILLIS, -1)
        // 连接socket服务器
        SocketClient.connect(host, port, connectTimeoutMillis, reconnectIntervalMillis)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SocketService", "onDestroy")
        SocketClient.disconnect()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("SocketService", "onBind")
        return null
    }

    companion object {
        private const val KEY_SOCKET_HOST = "socket_host"
        private const val KEY_SOCKET_PORT = "socket_port"
        private const val KEY_SOCKET_CONNECT_TIMEOUT_MILLIS = "socket_connectTimeoutMillis"
        private const val KEY_SOCKET_RECONNECT_INTERVAL_MILLIS = "socket_reconnectIntervalMillis"

        /**
         * 启动 Socket 服务并连接到服务器
         *
         * @param host                    服务器地址
         * @param port                    服务器端口
         * @param connectTimeoutMillis    连接超时时长，如果<=0，则默认10000毫秒
         * @param reconnectIntervalMillis 两次自动重连之间时间间隔，如果<=0，默认3000毫秒
         */
        fun connect(context: Context, host: String, port: Int, connectTimeoutMillis: Int, reconnectIntervalMillis: Long) {
            Log.i(
                "SocketService",
                "connect host=$host, port=$port, connectTimeoutMillis=$connectTimeoutMillis, reconnectIntervalMillis=$reconnectIntervalMillis"
            )
            val intent = Intent(context, SocketService::class.java)
            intent.putExtra(KEY_SOCKET_HOST, host)
            intent.putExtra(KEY_SOCKET_PORT, port)
            intent.putExtra(KEY_SOCKET_CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
            intent.putExtra(KEY_SOCKET_RECONNECT_INTERVAL_MILLIS, reconnectIntervalMillis)
            Log.i("SocketService", "connect startService=" + context.startService(intent))
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