package com.psk.device.data.model

import com.like.ble.util.toUUID
import java.io.Serializable
import java.util.UUID

/**
 * 通讯协议
 * @param isBeginOfPacket   是否一个完整数据包的开始。当返回true，则开始缓存接下来的数据，然后把组合起来的数据传递给isWhole。
 * @param isFullPacket      是否一个完整数据包。当返回true，则会移除通知监听并结束本挂起函数
 */
data class Protocol(
    val serviceUUID: UUID,
    val notifyUUID: UUID,
    val writeUUID: UUID,
    val isBeginOfPacket: ((ByteArray) -> Boolean)? = null,
    val isFullPacket: ((ByteArray) -> Boolean)? = null,
) {
    constructor(
        serviceUUIDString: String,
        notifyUUIDString: String,
        writeUUIDString: String,
        isBeginOfPacket: ((ByteArray) -> Boolean)? = null,
        isFullPacket: ((ByteArray) -> Boolean)? = null,
    ) : this(serviceUUIDString.toUUID(), notifyUUIDString.toUUID(), writeUUIDString.toUUID(), isBeginOfPacket, isFullPacket)

    override fun toString(): String {
        return "Protocol(serviceUUID=$serviceUUID, notifyUUID=$notifyUUID, writeUUID=$writeUUID)"
    }

}

/**
 * 设备类型
 */
enum class DeviceType(val des: String) : Serializable {
    BloodOxygen("血氧仪"), BloodPressure("血压计"), HeartRate("心电仪"), ShangXiaZhi("上下肢");
}
