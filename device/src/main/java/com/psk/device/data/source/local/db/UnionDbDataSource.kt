package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.UnionDao

class UnionDbDataSource(
    private val unionDao: UnionDao
) {

    suspend fun getAllOrderWithTime(): Map<Long, Long>? {
        return unionDao.getAllOrderWithTime()
    }

}
