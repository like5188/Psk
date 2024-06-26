package com.psk.shangxiazhi.data.source.local

import com.psk.shangxiazhi.data.db.dao.HealthInfoDao
import com.psk.shangxiazhi.data.model.HealthInfo

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
