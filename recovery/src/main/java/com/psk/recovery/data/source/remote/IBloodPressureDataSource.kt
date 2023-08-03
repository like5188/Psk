package com.psk.recovery.data.source.remote

import com.psk.recovery.data.model.BloodPressure

interface IBloodPressureDataSource : IRemoteDataSource {

    suspend fun fetch(medicalOrderId: Long): BloodPressure?

}
