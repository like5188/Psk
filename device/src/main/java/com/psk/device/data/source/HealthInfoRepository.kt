package com.psk.device.data.source

import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.model.HealthInfo
import com.psk.device.data.source.local.db.HealthInfoDbDataSource
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 健康信息数据仓库
 */
@OptIn(KoinApiExtension::class)
class HealthInfoRepository : KoinComponent {
    private val dbDataSource by lazy {
        HealthInfoDbDataSource(get<DeviceDatabase>().healthInfoDao())
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): HealthInfo? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)?.firstOrNull()
    }

    suspend fun insert(data: HealthInfo) {
        dbDataSource.insert(data)
    }

    suspend fun insertOrUpdate(data: HealthInfo) {
        dbDataSource.insertOrUpdate(data)
    }
}