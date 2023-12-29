package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.local.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.RKF_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.base.BaseShangXiaZhiDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * 上下肢数据仓库
 */
class ShangXiaZhiRepository : BaseBleDeviceRepository<BaseShangXiaZhiDataSource>(DeviceType.ShangXiaZhi) {
    private val dbDataSource by lazy {
        ShangXiaZhiDbDataSource(DeviceDatabaseManager.db.shangXiaZhiDao())
    }

    suspend fun getListByOrderId(orderId: Long): List<ShangXiaZhi>? {
        return dbDataSource.getByOrderId(orderId)
    }

    fun getFlow(scope: CoroutineScope, orderId: Long): Flow<ShangXiaZhi> {
        scope.launch(Dispatchers.IO) {
            bleDeviceDataSource.fetch(orderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    fun fetch(): Flow<ShangXiaZhi> {
        return bleDeviceDataSource.fetch(-1)
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

    suspend fun start(): Boolean {
        return bleDeviceDataSource.start()
    }

    suspend fun pause(): Boolean {
        return bleDeviceDataSource.pause()
    }

    suspend fun stop(): Boolean {
        return bleDeviceDataSource.stop()
    }

    suspend fun setParams(params: ShangXiaZhiParams): Boolean {
        return bleDeviceDataSource.setParams(params)
    }
}