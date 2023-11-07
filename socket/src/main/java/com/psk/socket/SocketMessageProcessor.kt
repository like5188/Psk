package com.psk.socket

import android.util.Log
import org.smartboot.socket.MessageProcessor
import org.smartboot.socket.StateMachineEnum
import org.smartboot.socket.transport.AioSession
import java.io.IOException
import java.nio.charset.StandardCharsets

class SocketMessageProcessor(private val socketListener: SocketListener) : MessageProcessor<String> {
    override fun process(session: AioSession, msg: String) {
        if (MsgType.PING.name == msg) {
            // 收到心跳消息，回复服务器（原样回复）
            if (session.isInvalid) {
                return
            }
            try {
                val bytes = msg.toByteArray(StandardCharsets.UTF_8)
                val outputStream = session.writeBuffer()
                outputStream.writeInt(bytes.size)
                outputStream.write(bytes)
                outputStream.flush()
                Log.v("SocketMessageProcessor", "发送心跳消息成功")
            } catch (e: IOException) {
                Log.e("SocketMessageProcessor", "发送心跳消息失败")
            }
            // 这里不能关闭，关了之后，连接就断了
            return
        }
        Log.v("SocketMessageProcessor", "收到非心跳消息:$msg")
        socketListener.onReceived(msg)
    }

    override fun stateEvent(session: AioSession, stateMachineEnum: StateMachineEnum, throwable: Throwable) {
        // 处理一些异常状态
        when (stateMachineEnum) {
            StateMachineEnum.SESSION_CLOSED -> {
                // 这个异常，需要重连，具体的可以看StateMachineEnum里面的说明
                // 重连
                Log.e("SocketMessageProcessor", "stateEvent 连接失败：$stateMachineEnum")
                socketListener.onDisConnected()
            }

            StateMachineEnum.NEW_SESSION -> {
                // 连接建立，
                Log.i("SocketMessageProcessor", "stateEvent 连接成功")
                socketListener.onConnected()
            }

            else -> Log.e("SocketMessageProcessor", throwable.message ?: "")
        }
    }
}