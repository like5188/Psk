package com.psk.device.data.source

import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.local.db.UnionDbDataSource
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 联合查询数据仓库
 */
@OptIn(KoinApiExtension::class)
class UnionRepository : KoinComponent {
    private val dbDataSource by lazy {
        UnionDbDataSource(get<DeviceDatabase>().unionDao())
    }

    suspend fun getAllMedicalOrderWithTime(): Map<Long, Long>? {
        return dbDataSource.getAllMedicalOrderWithTime()
    }
}