package com.psk.device.data.source

import com.psk.device.data.source.local.db.UnionDbDataSource

/**
 * 联合查询数据仓库
 */
class UnionRepository(
    private val unionDbDataSource: UnionDbDataSource
) {
    suspend fun getAllMedicalOrderWithTime(): Map<Long, Long>? {
        return unionDbDataSource.getAllMedicalOrderWithTime()
    }
}