package com.psk.recovery.data.source.remote

import com.psk.recovery.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

interface IHeartRateDataSource : IRemoteDataSource {

    suspend fun fetch(medicalOrderId: Long): Flow<HeartRate>

}
