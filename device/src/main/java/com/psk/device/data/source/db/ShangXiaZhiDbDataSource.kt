package com.psk.device.data.source.db

import com.psk.device.data.db.dao.ShangXiaZhiDao
import com.psk.device.data.model.ShangXiaZhi
import kotlinx.coroutines.flow.Flow

class ShangXiaZhiDbDataSource(
    private val shangXiaZhiDao: ShangXiaZhiDao
) {
    fun listenLatest(startTime: Long): Flow<ShangXiaZhi> {
        return shangXiaZhiDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun save(shangXiaZhi: ShangXiaZhi) {
        shangXiaZhiDao.insert(shangXiaZhi)
    }

}
