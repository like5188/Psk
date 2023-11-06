package com.psk.socket

import android.util.Log
import org.smartboot.socket.extension.protocol.StringProtocol
import org.smartboot.socket.transport.AioQuickClient
import org.smartboot.socket.transport.AioSession
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object SocketClient {
    private val SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor()
    private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

    // 两次自动重连之间时间间隔
    private var reconnectIntervalMillis: Long = 0
    var socketListener: SocketListener? = null
    private var client: AioQuickClient? = null
    private var session: AioSession? = null

    // -1：允许开始连接；0：允许自动重连；
    private val connectFlag = AtomicInteger(-1)
    private val connectRunnable = Runnable {
        Log.w("SocketClient", "尝试连接服务器...")
        try {
            session = client?.start()
        } catch (e: Exception) {
            Log.e("SocketClient", e.message ?: "")
            reConnect()
        }
    }

    /**
     * @param host                    服务器地址
     * @param port                    服务器端口
     * @param connectTimeoutMillis    连接超时时长，如果<=0，则默认10000毫秒
     * @param reconnectIntervalMillis 两次自动重连之间时间间隔，如果<=0，默认3000毫秒
     */
    @Synchronized
    fun connect(host: String?, port: Int, connectTimeoutMillis: Int, reconnectIntervalMillis: Long) {
        if (host.isNullOrEmpty() || port > 65535 || port <= 0) {
            Log.e("SocketClient", "connect 参数错误")
            return
        }
        if (connectFlag.compareAndSet(-1, 0)) {
            var c = connectTimeoutMillis
            if (c <= 0) {
                c = 10000
            }
            this.reconnectIntervalMillis = reconnectIntervalMillis
            if (this.reconnectIntervalMillis <= 0) {
                this.reconnectIntervalMillis = 3000
            }
            val listener: SocketListener = object : SocketListener {
                override fun onConnected() {
                    socketListener?.onConnected()
                }

                override fun onDisConnected() {
                    socketListener?.onDisConnected()
                    reConnect()
                }

                override fun onReceived(msg: String?) {
                    socketListener?.onReceived(msg)
                }
            }
            Log.i("SocketClient", "connect socket服务器地址：ip=$host,port=$port")
            client = AioQuickClient(host, port, StringProtocol(), SocketMessageProcessor(listener))
            client?.connectTimeout(c)
            IO_EXECUTOR.execute(connectRunnable)
        }
    }

    fun reConnect() {
        if (connectFlag.get() != 0) {
            return
        }
        SCHEDULE_EXECUTOR.schedule(connectRunnable, reconnectIntervalMillis, TimeUnit.MILLISECONDS)
    }

    @Synchronized
    fun disconnect() {
        Log.i("SocketService", "disconnect")
        if (connectFlag.compareAndSet(0, -1)) {
            session?.close()
        }
    }

    fun sendMsg(msg: String) {
        if (!isConnected) {
            Log.e("SocketClient", "sendMsg socket未连接，发送消息给服务器失败：$msg")
            return
        }
        try {
            val bytes = msg.toByteArray(StandardCharsets.UTF_8)
            val writeBuffer = session?.writeBuffer()
            writeBuffer?.writeInt(bytes.size)
            writeBuffer?.write(bytes)
            writeBuffer?.flush()
            Log.i("SocketClient", "sendMsg 发送消息给服务器成功：$msg")
        } catch (e: IOException) {
            Log.e("SocketClient", e.message ?: "")
        }
    }

    private val isConnected: Boolean
        get() = session?.isInvalid != true

}