package com.psk.device.data.source.remote

import com.psk.device.data.model.BloodOxygen

interface IBloodOxygenDataSource : IRemoteDeviceDataSource {

    suspend fun fetch(medicalOrderId: Long): BloodOxygen?

}
