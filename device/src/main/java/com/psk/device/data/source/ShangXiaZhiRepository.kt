package com.psk.device.data.source

import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.remote.RKF_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.base.BaseShangXiaZhiDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/**
 * 上下肢数据仓库
 */
@OptIn(KoinApiExtension::class)
class ShangXiaZhiRepository : KoinComponent, BaseBleDeviceRepository<ShangXiaZhi, BaseShangXiaZhiDataSource>(DeviceType.ShangXiaZhi) {

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<ShangXiaZhi> {
        scope.launch(Dispatchers.IO) {
            bleDeviceDataSource.fetch(medicalOrderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    fun setCallback(
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onOver: (() -> Unit)? = null,
    ) {
        (bleDeviceDataSource as? RKF_ShangXiaZhiDataSource)?.apply {
            this.onStart = onStart
            this.onPause = onPause
            this.onOver = onOver
        }
    }

    suspend fun resume() {
        bleDeviceDataSource.resume()
    }

    suspend fun pause() {
        bleDeviceDataSource.pause()
    }

    suspend fun over() {
        bleDeviceDataSource.over()
    }

    suspend fun setParams(params: ShangXiaZhiParams) {
        bleDeviceDataSource.setParams(params)
    }
}