package com.psk.device.data.source.remote.base

import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import kotlinx.coroutines.flow.Flow

abstract class BaseShangXiaZhiDataSource : BaseBleDeviceDataSource() {

    abstract fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi>

    /**
     * 开始运行
     */
    abstract suspend fun start()

    /**
     * 暂停运行
     */
    abstract suspend fun pause()

    /**
     * 停止运行
     */
    abstract suspend fun stop()

    abstract suspend fun setParams(params: ShangXiaZhiParams)

}
