package com.psk.device.data.source.remote

import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import kotlinx.coroutines.flow.Flow

abstract class BaseShangXiaZhiDataSource : BaseBleDeviceDataSource() {

    abstract suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi>

    /**
     * 使上下肢恢复运行
     */
    abstract suspend fun resume()

    /**
     * 使上下肢暂停运行
     */
    abstract suspend fun pause()

    /**
     * 使上下肢停止运行
     */
    abstract suspend fun over()

    abstract suspend fun setParams(params: ShangXiaZhiParams)

}
