package com.psk.device.data.source.remote.mock

import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.remote.IBloodPressureDataSource
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class MockBloodPressureDataSource : IBloodPressureDataSource {

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        thread {
            sleep(2000)
            onConnected()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): BloodPressure? {
        delay(5000)
        return BloodPressure(sbp = (90..140).random(), dbp = (60..90).random(), medicalOrderId = medicalOrderId)
    }

}
