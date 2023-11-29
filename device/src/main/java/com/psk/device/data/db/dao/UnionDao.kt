package com.psk.device.data.db.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class UnionDao {

    /**
     * 获取所有医嘱id及其开始时间（任意设备第一条数据存入数据库的时间）
     * @return key:orderId ; value:createTime;
     */
    suspend fun getAllOrderWithTime(): Map<Long, Long>? {
        val query =
            SimpleSQLiteQuery("SELECT orderId,createTime FROM (SELECT orderId,createTime FROM BloodOxygen UNION SELECT orderId,createTime FROM BloodPressure UNION SELECT orderId,createTime FROM HeartRate UNION SELECT orderId,createTime FROM ShangXiaZhi) AS a GROUP BY orderId ORDER BY createTime")
        return getAllOrderWithTime(query)
    }

    @MapInfo(keyColumn = "orderId", valueColumn = "createTime")
    @RawQuery
    protected abstract suspend fun getAllOrderWithTime(query: SupportSQLiteQuery): Map<Long, Long>?
}