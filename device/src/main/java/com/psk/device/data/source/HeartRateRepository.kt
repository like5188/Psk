package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.db.HeartRateDbDataSource
import com.psk.device.data.source.remote.base.BaseHeartRateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * 心率数据仓库
 */
class HeartRateRepository : BaseBleDeviceRepository<BaseHeartRateDataSource>(DeviceType.HeartRate) {
    private val dbDataSource by lazy {
        HeartRateDbDataSource(DeviceDatabaseManager.db.heartRateDao())
    }

    suspend fun getListByOrderId(orderId: Long): List<HeartRate>? {
        return dbDataSource.getByOrderId(orderId)
    }

    fun getFlow(scope: CoroutineScope, orderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            bleDeviceDataSource.fetch(orderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    fun fetch(): Flow<HeartRate> {
        return bleDeviceDataSource.fetch(-1)
    }

    fun getSampleRate(): Int {
        return bleDeviceDataSource.getSampleRate()
    }

}