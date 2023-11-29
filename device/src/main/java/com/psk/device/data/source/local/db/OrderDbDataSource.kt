package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.OrderDao
import com.psk.device.data.model.Order

class OrderDbDataSource(
    private val orderDao: OrderDao
) {

    suspend fun getAll(): List<Order>? {
        return orderDao.getAll()
    }

    suspend fun insert(data: Order) {
        orderDao.insert(data)
    }

}
