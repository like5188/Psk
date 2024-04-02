package com.psk.device.data.source.remote.socket.base

import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

abstract class BaseHeartRateDataSource : BaseSocketDeviceDataSource() {

    abstract fun fetch(orderId: Long): Flow<HeartRate>
    abstract fun getSampleRate(): Int
}
