package com.psk.device.data.source.remote.ble

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.Protocol
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.remote.IBloodOxygenDataSource
import com.psk.device.util.BleCRC

class O2_BloodOxygenDataSource(
    private val bleManager: BleManager
) : IBloodOxygenDataSource {
    private val protocol = Protocol(
        "14839ac4-7d7e-415c-9a42-167340cf2339",
        "0734594a-a8e7-4b1a-a6b1-cd5243059a57",
        "8b00ace7-eb0b-49b0-bbe9-9aee0a26e1a3",
        isBeginOfPacket = {
            it[0] == 0x55.toByte()
        }
    ) { it.last() == BleCRC.calCRC8(it) }
    private val device = Device("D5:92:D0:78:48:4E", protocol)

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        bleManager.addDevices(device)
        bleManager.connect(true, onConnected = {
            onConnected()
        }) {
            onDisconnected?.invoke()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): BloodOxygen? {
        val data = bleManager.writeAndWaitResult(device, "AA17E800000100002A")
        return if (data != null && data.size >= 8) {
            val bloodOxygen = if (data[7].toInt() < 0) {
                0
            } else {
                data[7].toInt()
            }
            BloodOxygen(value = bloodOxygen, medicalOrderId = medicalOrderId)
        } else {
            null
        }
    }

}
