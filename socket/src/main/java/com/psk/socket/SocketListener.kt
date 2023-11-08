package com.psk.socket

import java.nio.ByteBuffer

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