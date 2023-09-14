package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.HealthInfoDao
import com.psk.device.data.model.HealthInfo

class HealthInfoDbDataSource(
    private val healthInfoDao: HealthInfoDao
) {

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<HealthInfo>? {
        return healthInfoDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun insertOrUpdate(data: HealthInfo) {
        healthInfoDao.insertOrUpdate(data)
    }

}
