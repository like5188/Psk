package com.psk.device.data.source.remote.ble

import com.psk.device.DeviceType
import com.psk.device.Protocol
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import com.psk.device.util.BleCRC
import com.psk.device.util.BtResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.experimental.inv

class ER1_HeartRateDataSource : BaseHeartRateDataSource(DeviceType.HeartRate) {
    override val protocol = Protocol(
        "14839ac4-7d7e-415c-9a42-167340cf2339",
        "0734594A-A8E7-4B1A-A6B1-CD5243059A57",
        "8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3",
    )

    override suspend fun fetch(medicalOrderId: Long): Flow<HeartRate> = channelFlow {
        BtResponse.setReceiveListener(object : BtResponse.ReceiveListener {
            override fun onReceived(bleResponse: BtResponse.BleResponse?) {
                if (bleResponse?.cmd == 0x03) {
                    val rtData = BtResponse.RtData(bleResponse.content)
                    // 心率值
                    val heartRate = rtData.param.hr
                    // 心电图数据(这里经过测试数据量是不固定的，最大为128个)
                    val fs = rtData.wave.wFs
                    val coorYValues = if (fs == null || fs.isEmpty()) {
                        // 如果没有数据，就让心电图画y坐标为0的横线
                        (0..127).map { 0f }.toFloatArray()
                    } else {
                        fs
                    }
                    trySend(HeartRate(value = heartRate, coorYValues = coorYValues, medicalOrderId = medicalOrderId))
                }
            }
        })
        launch {
            // 延迟等待 setNotifyCallback 执行完成
            delay(100)
            while (isActive) {
                // 每1秒发送一次命令来获取心电相关的数据
                val start = System.currentTimeMillis()
                bleManager.write(device, getRtData())
                val cost = System.currentTimeMillis() - start
                val remain = 1000 - cost
                if (remain > 0) {
                    delay(remain)
                }
            }
        }
        var pool: ByteArray = byteArrayOf()
        bleManager.setNotifyCallback(device)?.collect {
            pool += it
            pool = BtResponse.hasResponse(pool) ?: byteArrayOf()
        }
    }

    private var seqNo = 0
    private fun getRtData(): ByteArray {
        val cmd = ByteArray(9)
        cmd[0] = 0xA5.toByte()
        cmd[1] = 0x03.toByte()
        cmd[2] = 0x03.toByte().inv()
        cmd[3] = 0x00.toByte()
        cmd[4] = seqNo.toByte()
        cmd[5] = 0x01.toByte()
        cmd[6] = 0x00.toByte()
        cmd[7] = 0x7D.toByte() // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = BleCRC.calCRC8(cmd)
        if (++seqNo >= 255) {
            seqNo = 0
        }
        return cmd
    }

}
