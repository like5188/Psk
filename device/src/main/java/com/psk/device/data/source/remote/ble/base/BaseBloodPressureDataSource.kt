package com.psk.device.data.source.remote.ble.base

import com.psk.device.data.model.BloodPressure

abstract class BaseBloodPressureDataSource : BaseBleDeviceDataSource() {

    /**
     * 等待手动操作血压计进行测量的结果
     */
    abstract suspend fun fetch(orderId: Long): BloodPressure?

    /**
     * 发送开始测量命令
     */
    abstract suspend fun measure(orderId: Long): BloodPressure?

    /**
     * 发送停止测量命令
     */
    abstract suspend fun stopMeasure()

    /**
     * 发送保持连接指令，使得血压计能保持不关机状态。
     */
    abstract suspend fun keepConnect(): Boolean
}
