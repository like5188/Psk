package com.psk.device.data.source.remote

import com.psk.device.data.model.BloodPressure

abstract class BaseBloodPressureDataSource : BaseBleDeviceDataSource() {

    /**
     * 等待手动操作血压计进行测量的结果
     */
    abstract suspend fun fetch(medicalOrderId: Long): BloodPressure?

    /**
     * 自动测量并返回结果
     */
    abstract suspend fun measure(medicalOrderId: Long): BloodPressure?

    /**
     * 向血压计发送保持连接指令，使得血压计能保持不关机状态。
     */
    abstract suspend fun keepConnect(): Boolean
}
