package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.OrderInfoDao
import com.psk.device.data.model.OrderInfo

class OrderInfoDbDataSource(
    private val orderInfoDao: OrderInfoDao
) {

    suspend fun getAll(): List<OrderInfo>? {
        return orderInfoDao.getAll()
    }

    suspend fun insert(data: OrderInfo) {
        orderInfoDao.insert(data)
    }

}
