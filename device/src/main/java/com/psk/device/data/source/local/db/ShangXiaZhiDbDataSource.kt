package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.ShangXiaZhiDao
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.local.IDbDataSource
import kotlinx.coroutines.flow.Flow

class ShangXiaZhiDbDataSource(
    private val shangXiaZhiDao: ShangXiaZhiDao
) : IDbDataSource<ShangXiaZhi> {
    override fun listenLatest(startTime: Long): Flow<ShangXiaZhi?> {
        return shangXiaZhiDao.listenLatest(startTime)
    }

    override suspend fun getByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDao.getByMedicalOrderId(medicalOrderId)
    }

    override suspend fun save(data: ShangXiaZhi) {
        shangXiaZhiDao.insert(data)
    }

}
