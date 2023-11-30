package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.db.ShangXiaZhiDatabaseManager
import com.psk.shangxiazhi.data.model.HealthInfo
import com.psk.shangxiazhi.data.source.local.HealthInfoDbDataSource

/**
 * 健康信息数据仓库
 */
class HealthInfoRepository {
    private val dbDataSource by lazy {
        HealthInfoDbDataSource(ShangXiaZhiDatabaseManager.db.healthInfoDao())
    }

    suspend fun getByOrderId(orderId: Long): HealthInfo? {
        return dbDataSource.getByOrderId(orderId)?.firstOrNull()
    }

    suspend fun insert(data: HealthInfo) {
        dbDataSource.insert(data)
    }

}