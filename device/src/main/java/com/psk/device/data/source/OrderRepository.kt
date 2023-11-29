package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.Order
import com.psk.device.data.source.local.db.OrderDbDataSource

/**
 * 训练数据仓库
 */
class OrderRepository {
    private val dbDataSource by lazy {
        OrderDbDataSource(DeviceDatabaseManager.db.orderDao())
    }

    suspend fun getAll(): List<Order>? {
        return dbDataSource.getAll()
    }

    suspend fun insert(data: Order) {
        dbDataSource.insert(data)
    }
}