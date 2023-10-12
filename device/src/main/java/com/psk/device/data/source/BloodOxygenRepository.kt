package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.local.db.BloodOxygenDbDataSource
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

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bleDeviceDataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.insert(this)
                }
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

}