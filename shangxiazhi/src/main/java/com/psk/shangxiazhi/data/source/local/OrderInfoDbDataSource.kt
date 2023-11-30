package com.psk.shangxiazhi.data.source.local

import com.psk.shangxiazhi.data.db.dao.OrderInfoDao
import com.psk.shangxiazhi.data.model.OrderInfo

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
