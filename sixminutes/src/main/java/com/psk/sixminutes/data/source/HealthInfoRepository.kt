package com.psk.sixminutes.data.source

import com.psk.sixminutes.data.db.SixMinutesDatabaseManager
import com.psk.sixminutes.data.model.HealthInfo
import com.psk.sixminutes.data.source.local.HealthInfoDbDataSource

/**
 * 健康信息数据仓库
 */
class HealthInfoRepository {
    private val dbDataSource by lazy {
        HealthInfoDbDataSource(SixMinutesDatabaseManager.db.healthInfoDao())
    }

    suspend fun getByOrderId(orderId: Long): HealthInfo? {
        return dbDataSource.getByOrderId(orderId)?.firstOrNull()
    }

    suspend fun insertOrUpdate(data: HealthInfo) {
        val orderId = data.orderId
        if (getByOrderId(orderId) == null) {
            println(dbDataSource.insert(data))
        } else {
            println(dbDataSource.update(data))
        }
    }

}