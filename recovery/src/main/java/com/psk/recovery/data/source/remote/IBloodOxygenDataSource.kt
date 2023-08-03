package com.psk.recovery.data.source.remote

import com.psk.recovery.data.model.BloodOxygen

interface IBloodOxygenDataSource : IRemoteDataSource {

    suspend fun fetch(medicalOrderId: Long): BloodOxygen?

}
