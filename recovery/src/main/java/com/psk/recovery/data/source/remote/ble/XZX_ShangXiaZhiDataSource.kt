package com.psk.recovery.data.source.remote.ble

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.Protocol
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.data.source.remote.IShangXiaZhiDataSource
import com.psk.recovery.util.ShangXiaZhiParser
import com.psk.recovery.util.ShangXiaZhiReceiver
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
    private val shangXiaZhiParser by lazy {
        ShangXiaZhiParser()
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
        shangXiaZhiParser.setReceiver(object : ShangXiaZhiReceiver {
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
                trySend(shangXiaZhi)
            }

            override fun onPause() {
                println("onPause")
            }

            override fun onOver() {
                println("onOver")
            }

        })
        bleManager.setNotifyCallback(device)?.collect {
            shangXiaZhiParser.putData(it)
        }
    }

}
