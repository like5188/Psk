package com.psk.device.data.source.remote.mock

import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.remote.IShangXiaZhiDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlin.concurrent.thread

class MockShangXiaZhiDataSource : IShangXiaZhiDataSource {
    var offset = 0
    override fun isConnected(): Boolean {
        return true
    }

    override suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi> = channelFlow {
        while (isActive) {
            delay(1000)
            val shangXiaZhi = ShangXiaZhi(
                model = 1,
                speedLevel = (1..12).random(),
                speedValue = (5..60).random(),
                offset = offset++,
                spasmNum = 1,
                spasmLevel = 1,
                res = 1,
                intelligence = 1,
                direction = 1,
            )
            if (offset > 30) {
                offset = 0
            }
            trySend(shangXiaZhi)
        }
    }

    override suspend fun resume() {
        println("控制上下肢：resume")
    }

    override suspend fun pause() {
        println("控制上下肢：pause")
    }

    override suspend fun over() {
        println("控制上下肢：over")
    }

    override suspend fun setParams(
        passiveModule: Boolean,
        timeInt: Int,
        speedInt: Int,
        spasmInt: Int,
        resistanceInt: Int,
        intelligent: Boolean,
        turn2: Boolean
    ) {
        println("控制上下肢：setParams")
    }

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        thread {
            Thread.sleep(3000)
            onConnected()
        }
    }

}
