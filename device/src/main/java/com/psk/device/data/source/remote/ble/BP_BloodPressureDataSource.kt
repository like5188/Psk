package com.psk.device.data.source.remote.ble

import com.psk.ble.DeviceType
import com.psk.ble.Protocol
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.remote.BaseBloodPressureDataSource

/**
 * maibobo 血压计数据源
 */
class BP_BloodPressureDataSource : BaseBloodPressureDataSource(DeviceType.BloodPressure) {
    override val protocol: Protocol = Protocol(
        "0000fff0-0000-1000-8000-00805f9b34fb",
        "0000fff1-0000-1000-8000-00805f9b34fb",
        "0000fff2-0000-1000-8000-00805f9b34fb",
        isBeginOfPacket = {
            it[0] == 0xAA.toByte()
        }
    ) { it.size == 20 }

    override suspend fun fetch(medicalOrderId: Long): BloodPressure? {
        val data = bleManager.waitResult(device)
        return if (data != null && data.size >= 17) {
            // 高8位左移8位+低8位。比如：高8位(0x01),低8位(0x78)。结果：0x01 shl 8 + 0x78 = 256 + 120 = 376
            val v0: Int = data[13].toInt() and 0xff shl 8
            val v1: Int = data[14].toInt() and 0xff
            val sbp: Int = v0 + v1
            val v2: Int = data[15].toInt() and 0xff shl 8
            val v3: Int = data[16].toInt() and 0xff
            val dbp: Int = v2 + v3
            BloodPressure(sbp = sbp, dbp = dbp, medicalOrderId = medicalOrderId)
        } else {
            null
        }
    }

}
