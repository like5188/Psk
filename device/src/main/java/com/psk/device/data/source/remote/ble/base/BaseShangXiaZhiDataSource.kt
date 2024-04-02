package com.psk.device.data.source.remote.ble.base

import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import kotlinx.coroutines.flow.Flow

abstract class BaseShangXiaZhiDataSource : BaseBleDeviceDataSource() {

    abstract fun fetch(orderId: Long): Flow<ShangXiaZhi>

    /**
     * 开始运行
     */
    abstract suspend fun start(): Boolean

    /**
     * 暂停运行
     */
    abstract suspend fun pause(): Boolean

    /**
     * 停止运行
     */
    abstract suspend fun stop(): Boolean

    abstract suspend fun setParams(params: ShangXiaZhiParams): Boolean

}
