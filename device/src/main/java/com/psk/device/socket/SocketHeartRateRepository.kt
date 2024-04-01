package com.psk.device.socket

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.HeartRateDbDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Socket心率数据仓库
 */
class SocketHeartRateRepository {
    private val dbDataSource by lazy {
        HeartRateDbDataSource(DeviceDatabaseManager.db.heartRateDao())
    }
    private val socketDeviceDataSource by lazy {
        ICV200A_HeartRateDataSource()
    }

    suspend fun getListByOrderId(orderId: Long): List<HeartRate>? {
        return dbDataSource.getByOrderId(orderId)
    }

    /**
     * 注意返回的是热流
     */
    fun getFlow(scope: CoroutineScope, orderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            socketDeviceDataSource.fetch(orderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    /**
     * 注意返回的是冷流
     */
    fun fetch(): Flow<HeartRate> {
        return socketDeviceDataSource.fetch(-1)
    }

    fun getSampleRate(): Int {
        return socketDeviceDataSource.getSampleRate()
    }

}