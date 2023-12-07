package com.psk.device.data.source.remote

import com.like.common.util.Logger
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.Protocol
import com.psk.device.data.source.remote.base.BaseHeartRateDataSource
import com.psk.device.util.BleCRC
import com.psk.device.util.BtResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.experimental.inv

/**
 * 乐普单导联动态心电记录仪数据源
 * 经测试，采样率为128，但是需要自己主动去获取数据，如果获取数据的时间间隔不足够1秒的话，获取到的数据量就达不到128
 */
class ER1_HeartRateDataSource : BaseHeartRateDataSource() {
    override val protocol = Protocol(
        "14839ac4-7d7e-415c-9a42-167340cf2339",
        "0734594A-A8E7-4B1A-A6B1-CD5243059A57",
        "8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3",
    )

    override fun fetch(orderId: Long): Flow<HeartRate> = channelFlow {
        BtResponse.setReceiveListener(object : BtResponse.ReceiveListener {
            override fun onReceived(bleResponse: BtResponse.BleResponse?) {
                if (bleResponse?.cmd == 0x03) {
                    val rtData = BtResponse.RtData(bleResponse.content)
                    // 心率值
                    val heartRate = rtData.param.hr
                    // 心电图数据(最大为128个)
                    val fs = rtData.wave.wFs
                    println("heartRate=$heartRate size=${fs?.size}")
                    val coorYValues = if (fs == null || fs.isEmpty()) {
                        // 如果没有数据，就让心电图画y坐标为0的横线
                        (0..127).map { 0f }.toFloatArray()
                    } else {
                        fs
                    }
                    trySend(HeartRate(value = heartRate, coorYValues = coorYValues, orderId = orderId))
                }
            }
        })
        launch {
            while (isActive) {
                // 这里延迟太短会造成心电设备异常停止。延迟太长会造成每秒时长误差太大。
                // 第一次延迟是为了等待 setNotifyCallback 执行完成
                delay(1000)
                write(getRtData())
            }
        }
        Logger.i("ER1_HeartRateDataSource setNotifyCallback")
        var pool: ByteArray = byteArrayOf()
        setNotifyCallback().collect {
            pool += it
            pool = BtResponse.hasResponse(pool) ?: byteArrayOf()
        }
    }.flowOn(Dispatchers.IO)

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

    override fun getSampleRate(): Int {
        return 128
    }

}
