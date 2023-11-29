package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.ShangXiaZhiDao
import com.psk.device.data.model.ShangXiaZhi
import kotlinx.coroutines.flow.Flow

class ShangXiaZhiDbDataSource(
    private val shangXiaZhiDao: ShangXiaZhiDao
) {
    fun listenLatest(startTime: Long): Flow<ShangXiaZhi?> {
        return shangXiaZhiDao.listenLatest(startTime)
    }

    suspend fun getByOrderId(orderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDao.getByOrderId(orderId)
    }

    suspend fun insert(data: ShangXiaZhi) {
        shangXiaZhiDao.insert(data)
    }

}
