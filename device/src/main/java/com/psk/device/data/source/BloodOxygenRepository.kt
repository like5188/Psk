package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.local.BloodOxygenDbDataSource
import com.psk.device.data.source.remote.base.BaseBloodOxygenDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 血氧数据仓库
 */
class BloodOxygenRepository : BaseBleDeviceRepository<BaseBloodOxygenDataSource>(DeviceType.BloodOxygen) {
    private val dbDataSource by lazy {
        BloodOxygenDbDataSource(DeviceDatabaseManager.db.bloodOxygenDao())
    }

    suspend fun getListByOrderId(orderId: Long): List<BloodOxygen>? {
        return dbDataSource.getByOrderId(orderId)
    }

    /**
     * 注意返回的是热流
     */
    fun getFlow(scope: CoroutineScope, orderId: Long, interval: Long): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bleDeviceDataSource.fetch(orderId)?.apply {
                    dbDataSource.insert(this)
                }
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

}