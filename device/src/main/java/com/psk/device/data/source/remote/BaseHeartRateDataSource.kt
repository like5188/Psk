package com.psk.device.data.source.remote

import com.psk.device.DeviceType
import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

abstract class BaseHeartRateDataSource(deviceType: DeviceType) : BaseRemoteDeviceDataSource(deviceType) {

    abstract suspend fun fetch(medicalOrderId: Long): Flow<HeartRate>

}
