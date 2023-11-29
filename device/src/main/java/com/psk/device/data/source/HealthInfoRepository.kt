package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.HealthInfo
import com.psk.device.data.source.local.db.HealthInfoDbDataSource

/**
 * 健康信息数据仓库
 */
class HealthInfoRepository {
    private val dbDataSource by lazy {
        HealthInfoDbDataSource(DeviceDatabaseManager.db.healthInfoDao())
    }

    suspend fun getByOrderId(orderId: Long): HealthInfo? {
        return dbDataSource.getByOrderId(orderId)?.firstOrNull()
    }

    suspend fun insert(data: HealthInfo) {
        dbDataSource.insert(data)
    }

}