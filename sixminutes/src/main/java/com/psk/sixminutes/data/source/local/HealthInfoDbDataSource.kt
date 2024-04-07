package com.psk.sixminutes.data.source.local

import com.psk.sixminutes.data.db.dao.HealthInfoDao
import com.psk.sixminutes.data.model.HealthInfo

class HealthInfoDbDataSource(private val healthInfoDao: HealthInfoDao) {

    suspend fun getByOrderId(orderId: Long): List<HealthInfo>? {
        return healthInfoDao.getByOrderId(orderId)
    }

    suspend fun insert(data: HealthInfo) {
        healthInfoDao.insert(data)
    }

    suspend fun update(data: HealthInfo) {
        healthInfoDao.update(data)
    }

}
