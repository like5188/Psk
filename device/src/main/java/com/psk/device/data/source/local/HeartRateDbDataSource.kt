package com.psk.device.data.source.local

import com.psk.device.data.db.dao.HeartRateDao
import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

class HeartRateDbDataSource(
    private val heartRateDao: HeartRateDao
) {
    fun listenLatest(startTime: Long): Flow<HeartRate?> {
        return heartRateDao.listenLatest(startTime)
    }

    suspend fun getByOrderId(orderId: Long): List<HeartRate>? {
        return heartRateDao.getByOrderId(orderId)
    }

    suspend fun insert(data: HeartRate) {
        heartRateDao.insert(data)
    }

}
