package com.psk.device

import com.like.ble.util.toUUID
import com.psk.device.data.source.remote.ble.BP_BloodPressureDataSource
import com.psk.device.data.source.remote.ble.ER1_HeartRateDataSource
import com.psk.device.data.source.remote.ble.O2_BloodOxygenDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.SCI311W_HeartRateDataSource
import java.io.Serializable
import java.util.UUID

/**
 * 设备信息
 *
 * @param address       设备地址
 * @param protocol      通讯协议
 * @param type          设备类型
 */
data class Device(
    val address: String, val protocol: Protocol, val type: DeviceType
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

}

/**
 * 通讯协议
 * @param isBeginOfPacket   是否一个完整数据包的开始。当返回true，则开始缓存接下来的数据，然后把组合起来的数据传递给isWhole。
 * @param isFullPacket      是否一个完整数据包。当返回true，则会移除通知监听并结束本挂起函数
 * 以上两个参数在需要调用[BleManager.waitResult]、[BleManager.writeAndWaitResult]方法时需要传递
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
enum class DeviceType : Serializable {
    BloodOxygen, BloodPressure, HeartRate, ShangXiaZhi;

    /**
     * 当前设备类型是否包含指定设备
     *
     * @param deviceName    设备名称
     */
    fun contains(deviceName: String?): Boolean {
        if (deviceName.isNullOrEmpty()) {
            return false
        }
        return when (this) {
            BloodOxygen -> deviceName.startsWith(O2_BloodOxygenDataSource.NAME_PREFIX)
            BloodPressure -> deviceName.startsWith(BP_BloodPressureDataSource.NAME_PREFIX)
            HeartRate -> deviceName.startsWith(ER1_HeartRateDataSource.NAME_PREFIX) || deviceName.startsWith(SCI311W_HeartRateDataSource.NAME_PREFIX)
            ShangXiaZhi -> deviceName.startsWith(RKF_ShangXiaZhiDataSource.NAME_PREFIX)
        }
    }
}
