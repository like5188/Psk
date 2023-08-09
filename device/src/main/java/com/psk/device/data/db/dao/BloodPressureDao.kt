package com.psk.device.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.psk.device.data.model.BloodPressure
import kotlinx.coroutines.flow.Flow

@Dao
abstract class BloodPressureDao : BaseDao<BloodPressure>() {
    /**
     * 监听[startTime]开始的最近的 1 条数据，如果表中的任何行有更新，则重新查询并发射最新数据
     */
    @Query("SELECT * FROM BloodPressure WHERE time >= :startTime ORDER BY id DESC LIMIT 1")
    abstract fun listenLatest(startTime: Long): Flow<BloodPressure>
}