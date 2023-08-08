package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.ShangXiaZhiDao
import com.psk.recovery.data.model.ShangXiaZhi
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
