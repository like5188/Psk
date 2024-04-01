package com.psk.device.socket.remote.base

import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

abstract class BaseSocketHeartRateDataSource : BaseSocketDeviceDataSource() {

    abstract fun fetch(orderId: Long): Flow<HeartRate>
    abstract fun getSampleRate(): Int
}
