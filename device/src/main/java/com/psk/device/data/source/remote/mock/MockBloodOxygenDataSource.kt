package com.psk.device.data.source.remote.mock

import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.remote.IBloodOxygenDataSource
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class MockBloodOxygenDataSource : IBloodOxygenDataSource {

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        thread {
            sleep(1000)
            onConnected()
            sleep(5000)
            onDisconnected?.invoke()
            sleep(3000)
            onConnected()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): BloodOxygen? {
        delay(100)
        return BloodOxygen(value = (95..100).random(), medicalOrderId = medicalOrderId)
    }

}
