package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.OrderInfo
import com.psk.device.data.source.local.db.OrderInfoDbDataSource

/**
 * 训练数据仓库
 */
class OrderInfoRepository {
    private val dbDataSource by lazy {
        OrderInfoDbDataSource(DeviceDatabaseManager.db.orderInfoDao())
    }

    suspend fun getAll(): List<OrderInfo>? {
        return dbDataSource.getAll()
    }

    suspend fun insert(data: OrderInfo) {
        dbDataSource.insert(data)
    }
}