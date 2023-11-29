package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.HealthInfoDao
import com.psk.device.data.model.HealthInfo

class HealthInfoDbDataSource(
    private val healthInfoDao: HealthInfoDao
) {

    suspend fun getByOrderId(orderId: Long): List<HealthInfo>? {
        return healthInfoDao.getByOrderId(orderId)
    }

    suspend fun insert(data: HealthInfo) {
        healthInfoDao.insert(data)
    }

}
