package com.psk.recovery.data.source.remote.ble

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.Protocol
import com.psk.recovery.data.model.BloodPressure
import com.psk.recovery.data.source.remote.IBloodPressureDataSource

class BP88B180704_BloodPressureDataSource(
    private val bleManager: BleManager
) : IBloodPressureDataSource {
    private val protocol = Protocol(
        "0000fff0-0000-1000-8000-00805f9b34fb",
        "0000fff1-0000-1000-8000-00805f9b34fb",
        "0000fff2-0000-1000-8000-00805f9b34fb",
        isBeginOfPacket = {
            it[0] == 0xAA.toByte()
        }
    ) { it.size == 20 }
    private val device = Device("88:1B:99:0B:78:D3", protocol)

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        bleManager.addDevices(device)
        bleManager.connect(true, onConnected = {
            onConnected()
        }) {
            onDisconnected?.invoke()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): BloodPressure? {
        val data = bleManager.waitResult(device)
        return if (data != null && data.size >= 17) {
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
