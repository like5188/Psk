package com.psk.device.data.source.remote

import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodPressure

abstract class BaseBloodPressureDataSource(deviceType: DeviceType) : BaseRemoteDeviceDataSource(deviceType) {

    /**
     * 等待手动操作血压计进行测量的结果
     */
    abstract suspend fun fetch(medicalOrderId: Long): BloodPressure?

    /**
     * 自动测量并返回结果
     */
    abstract suspend fun measure(): BloodPressure?
}
