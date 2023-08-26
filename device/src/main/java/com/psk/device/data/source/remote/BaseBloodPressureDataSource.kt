package com.psk.device.data.source.remote

import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodPressure

abstract class BaseBloodPressureDataSource(deviceType: DeviceType) : BaseRemoteDeviceDataSource(deviceType) {

    abstract suspend fun fetch(medicalOrderId: Long): BloodPressure?

}
