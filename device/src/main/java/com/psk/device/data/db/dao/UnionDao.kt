package com.psk.device.data.db.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class UnionDao {

    /**
     * 获取所有医嘱及其开始时间（任意设备第一条数据存入数据库的时间）
     * @return key:medicalOrderId ; value:createTime;
     */
    suspend fun getAllMedicalOrderWithTime(): Map<Long, Long>? {
        val query =
            SimpleSQLiteQuery("SELECT medicalOrderId,createTime FROM (SELECT medicalOrderId,createTime FROM BloodOxygen UNION SELECT medicalOrderId,createTime FROM BloodPressure UNION SELECT medicalOrderId,createTime FROM HeartRate UNION SELECT medicalOrderId,createTime FROM ShangXiaZhi) AS a GROUP BY medicalOrderId ORDER BY createTime")
        return getAllMedicalOrderWithTime(query)
    }

    @MapInfo(keyColumn = "medicalOrderId", valueColumn = "createTime")
    @RawQuery
    protected abstract suspend fun getAllMedicalOrderWithTime(query: SupportSQLiteQuery): Map<Long, Long>?
}