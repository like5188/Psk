package com.psk.device.data.source.remote

import com.psk.device.DeviceType
import com.psk.device.data.model.BloodOxygen

abstract class BaseBloodOxygenDataSource(deviceType: DeviceType) : BaseRemoteDeviceDataSource(deviceType) {

    abstract suspend fun fetch(medicalOrderId: Long): BloodOxygen?

}
