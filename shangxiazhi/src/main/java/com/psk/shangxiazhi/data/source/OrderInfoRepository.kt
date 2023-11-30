package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.db.ShangXiaZhiDatabaseManager
import com.psk.shangxiazhi.data.model.OrderInfo
import com.psk.shangxiazhi.data.source.local.OrderInfoDbDataSource

/**
 * 训练数据仓库
 */
class OrderInfoRepository {
    private val dbDataSource by lazy {
        OrderInfoDbDataSource(ShangXiaZhiDatabaseManager.db.orderInfoDao())
    }

    suspend fun getAll(): List<OrderInfo>? {
        return dbDataSource.getAll()
    }

    suspend fun insert(data: OrderInfo) {
        dbDataSource.insert(data)
    }
}