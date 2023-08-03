package com.psk.recovery.data.source.remote.mock

import com.psk.recovery.data.model.HeartRate
import com.psk.recovery.data.source.remote.IHeartRateDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import java.lang.Thread.sleep
import java.math.BigDecimal
import kotlin.concurrent.thread

class MockHeartRateDataSource : IHeartRateDataSource {

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        thread {
            sleep(3000)
            onConnected()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): Flow<HeartRate> = channelFlow {
        val count = 125// count 为心电数据采样率，即 1 秒钟采集 count 次数据。
        while (isActive) {
            val heartRate = IntArray(count)
            val coorYValues = FloatArray(count)
            (0 until count).forEach {
                delay(1000L / count)
                heartRate[it] = (60..100).random()
                coorYValues[it] =
                    BigDecimal.valueOf((-128..127).random().toDouble()).setScale(5, BigDecimal.ROUND_HALF_DOWN).toFloat() / 150f
            }
            trySend(HeartRate(values = heartRate, coorYValues = coorYValues, medicalOrderId = medicalOrderId))
        }
    }

}
