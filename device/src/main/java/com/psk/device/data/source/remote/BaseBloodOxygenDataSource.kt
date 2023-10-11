package com.psk.device.data.source.remote

import com.psk.device.data.model.BloodOxygen

abstract class BaseBloodOxygenDataSource : BaseBleDeviceDataSource() {

    abstract suspend fun fetch(medicalOrderId: Long): BloodOxygen?

}
