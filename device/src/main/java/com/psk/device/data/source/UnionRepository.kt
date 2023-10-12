package com.psk.device.data.source

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.source.local.db.UnionDbDataSource

/**
 * 联合查询数据仓库
 */
class UnionRepository {
    private val dbDataSource by lazy {
        UnionDbDataSource(DeviceDatabaseManager.db.unionDao())
    }

    suspend fun getAllMedicalOrderWithTime(): Map<Long, Long>? {
        return dbDataSource.getAllMedicalOrderWithTime()
    }
}