package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.HeartRateDao
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.IDbDataSource
import kotlinx.coroutines.flow.Flow

class HeartRateDbDataSource(
    private val heartRateDao: HeartRateDao
) : IDbDataSource<HeartRate> {
    override fun listenLatest(startTime: Long): Flow<HeartRate?> {
        return heartRateDao.listenLatest(startTime)
    }

    override suspend fun getAll(): List<HeartRate>? {
        return heartRateDao.getAll()
    }

    override suspend fun getByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDao.getByMedicalOrderId(medicalOrderId)
    }

    override suspend fun save(data: HeartRate) {
        heartRateDao.insert(data)
    }

}
