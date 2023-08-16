package com.psk.device.data.source.remote.ble

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.Protocol
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.remote.IShangXiaZhiDataSource
import com.psk.device.util.ShangXiaZhiDataParser
import com.psk.device.util.ShangXiaZhiReceiver
import com.twsz.remotecommands.RemoteCommand
import com.twsz.remotecommands.TrunkCommandData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class XZX_ShangXiaZhiDataSource(
    private val bleManager: BleManager
) : IShangXiaZhiDataSource {
    private val protocol = Protocol(
        "0000ffe1-0000-1000-8000-00805f9b34fb",
        "0000ffe2-0000-1000-8000-00805f9b34fb",
        "0000ffe3-0000-1000-8000-00805f9b34fb",
    )
    private val device = Device("00:1B:10:3A:01:2C", protocol)
    private val shangXiaZhiDataParser by lazy {
        ShangXiaZhiDataParser()
    }

    // 上下肢开始运行或者从暂停中恢复运行时回调
    var onStart: (() -> Unit)? = null

    // 上下肢暂停时回调
    var onPause: (() -> Unit)? = null

    // 上下肢结束时回调
    var onOver: (() -> Unit)? = null

    override fun isConnected(): Boolean {
        return bleManager.isConnected(device)
    }

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        bleManager.addDevices(device)
        bleManager.connect(true, onConnected = {
            onConnected()
        }) {
            onDisconnected?.invoke()
        }
    }

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
        bleManager.write(
            device, RemoteCommand.generateParam(TrunkCommandData().apply {
                this.model = model
                this.time = time
                this.speed = speed
                this.spasm = spasm
                this.intelligence = intelligence
                this.resistance = resistance
                this.direction = direction
            })
        )
    }

}
