package com.psk.sixminutes.model

import com.psk.device.data.model.DeviceType
import java.io.Serializable

sealed class Info : Serializable

data class BleInfo(val deviceType: DeviceType, val name: String, val address: String) : Info()

/**
 * @param hostName  本服务器开放的地址。默认为 null，表示需要连接的设备与本服务器处于局域网。
 * @param port      本服务器开放的端口号。默认为 7777。
 */
data class SocketInfo(val deviceType: DeviceType, val name: String, val hostName: String? = null, val port: Int) : Info()