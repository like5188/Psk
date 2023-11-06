package com.psk.socket

/*
HEART（心跳）
ONLINE（上线消息）
OFFLINE（下线消息）
MSG（普通消息）
 */
enum class MsgType {
    HEART, ONLINE, OFFLINE, MSG
}