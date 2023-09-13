package com.psk.device.data.source

import com.psk.device.data.model.HealthInfo
import com.psk.device.data.source.local.db.HealthInfoDbDataSource

/**
 * 健康信息数据仓库
 */
class HealthInfoRepository(
    private val healthInfoDbDataSource: HealthInfoDbDataSource
) {
    suspend fun getByMedicalOrderId(medicalOrderId: Long): HealthInfo? {
        return healthInfoDbDataSource.getByMedicalOrderId(medicalOrderId)?.firstOrNull()
    }

    suspend fun save(data: HealthInfo) {
        healthInfoDbDataSource.save(data)
    }
}