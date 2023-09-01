package com.psk.device.data.source.remote.ble

import com.like.ble.util.isBleDeviceConnected
import com.psk.ble.DeviceType
import com.psk.ble.Protocol
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.remote.BaseShangXiaZhiDataSource
import com.psk.device.util.ShangXiaZhiDataParser
import com.psk.device.util.ShangXiaZhiReceiver
import com.twsz.remotecommands.RemoteCommand
import com.twsz.remotecommands.TrunkCommandData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

/**
 * 瑞甲上下肢康复机数据源
 */
class RKF_ShangXiaZhiDataSource : BaseShangXiaZhiDataSource(DeviceType.ShangXiaZhi) {
    override val protocol = Protocol(
        "0000ffe1-0000-1000-8000-00805f9b34fb",
        "0000ffe2-0000-1000-8000-00805f9b34fb",
        "0000ffe3-0000-1000-8000-00805f9b34fb",
    )
    private val shangXiaZhiDataParser by lazy {
        ShangXiaZhiDataParser()
    }

    // 上下肢开始运行或者从暂停中恢复运行时回调
    var onStart: (() -> Unit)? = null

    // 上下肢暂停时回调
    var onPause: (() -> Unit)? = null

    // 上下肢结束时回调
    var onOver: (() -> Unit)? = null

    override suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi> = channelFlow {
        shangXiaZhiDataParser.receiver = object : ShangXiaZhiReceiver {
            override fun onReceive(
                model: Byte,
                speedLevel: Int,
                speedValue: Int,
                offset: Int,
                spasmNum: Int,
                spasmLevel: Int,
                res: Int,
                intelligence: Byte,
                direction: Byte
            ) {
                val shangXiaZhi = ShangXiaZhi(
                    model = model,
                    speedLevel = speedLevel,
                    speedValue = speedValue,
                    offset = offset,
                    spasmNum = spasmNum,
                    spasmLevel = spasmLevel,
                    res = res,
                    intelligence = intelligence,
                    direction = direction,
                )
                onStart?.invoke()
                trySend(shangXiaZhi)
            }

            override fun onPause() {
                onPause?.invoke()
            }

            override fun onOver() {
                onOver?.invoke()
            }

        }
        bleManager.setNotifyCallback(device)?.collect {
            shangXiaZhiDataParser.putData(it)
        }
    }

    override suspend fun resume() {
        bleManager.write(device, RemoteCommand.generateStartParam())
    }

    override suspend fun pause() {
        bleManager.write(device, RemoteCommand.generatePauseParam())
    }

    override suspend fun over() {
        bleManager.write(device, RemoteCommand.generateStopParam())
    }

    override suspend fun setParams(
        passiveModule: Boolean, timeInt: Int, speedInt: Int, spasmInt: Int, resistanceInt: Int, intelligent: Boolean, turn2: Boolean
    ) {
        //被动
        val model = if (passiveModule) {
            0x01.toByte()
        } else {
            0x02.toByte()
        }

        val time = timeInt.toByte()
        val speed = (speedInt / 5).toByte()
        val spasm = spasmInt.toByte()
        val resistance = resistanceInt.toByte()

        //智能
        val intelligence = if (intelligent) {
            0x00.toByte()
        } else {
            0x01.toByte()
        }

        val direction = if (turn2) {
            0x00.toByte()
        } else {
            0x01.toByte()
        }
        if (!context.isBleDeviceConnected(device.address)) {
            // 如果是未连接，则不管，因为连接成功后，会调用 setParams() 方法。
            return
        }
        // 如果已经连接，就必须写入成功，否则上下肢无法运动。
        val cmd: ByteArray = RemoteCommand.generateParam(TrunkCommandData().apply {
            this.model = model
            this.time = time
            this.speed = speed
            this.spasm = spasm
            this.intelligence = intelligence
            this.resistance = resistance
            this.direction = direction
        })
        var result = false
        while (!result) {
            result = bleManager.write(device, cmd)
            if (!result) {
                delay(100)
            }
        }
        println("RKF_ShangXiaZhiDataSource setParams success")
    }

}
