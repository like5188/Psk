package com.psk.device.data.source.remote

import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

interface IHeartRateDataSource : IRemoteDeviceDataSource {

    suspend fun fetch(medicalOrderId: Long): Flow<HeartRate>

}
