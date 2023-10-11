package com.psk.device.data.source.remote.ble.base

import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

abstract class BaseHeartRateDataSource : BaseBleDeviceDataSource() {

    abstract suspend fun fetch(medicalOrderId: Long): Flow<HeartRate>

}
