package com.psk.device.data.source.remote.base

import com.psk.device.data.model.BloodOxygen

abstract class BaseBloodOxygenDataSource : BaseBleDeviceDataSource() {

    abstract suspend fun fetch(medicalOrderId: Long): BloodOxygen?

}
