package com.psk.device.data.source.remote

import com.psk.device.data.model.BloodPressure

interface IBloodPressureDataSource : IRemoteDeviceDataSource {

    suspend fun fetch(medicalOrderId: Long): BloodPressure?

}
