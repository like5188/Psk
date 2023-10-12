package com.psk.device.data.source.remote.base

import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

abstract class BaseHeartRateDataSource : BaseBleDeviceDataSource() {

    abstract fun fetch(medicalOrderId: Long): Flow<HeartRate>

}
