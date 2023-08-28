package com.psk.device.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.psk.device.data.model.BloodOxygen
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BloodOxygenDao : BaseDao<BloodOxygen>() {
    /**
     * 监听[startTime]开始的最近的 1 条数据，如果表中的任何行有更新，则重新查询并发射最新数据
     * 注意：数据有可能为 null。
     */
    @Query("SELECT * FROM BloodOxygen WHERE time >= :startTime ORDER BY id DESC LIMIT 1")
    abstract fun listenLatest(startTime: Long): Flow<BloodOxygen?>
}