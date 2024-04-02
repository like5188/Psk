package com.psk.device.data.source.remote.ble.base

import com.psk.device.data.model.BloodOxygen

abstract class BaseBloodOxygenDataSource : BaseBleDeviceDataSource() {

    abstract suspend fun fetch(orderId: Long): BloodOxygen?

}
