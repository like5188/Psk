package com.psk.socket

interface SocketListener {
    /**
     * 连接成功回调
     */
    fun onConnected()

    /**
     * 连接失败回调
     */
    fun onDisConnected()

    /**
     * 接收到消息
     */
    fun onReceived(msg: Msg?)
}