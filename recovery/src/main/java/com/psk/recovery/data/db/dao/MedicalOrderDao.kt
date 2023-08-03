package com.psk.recovery.data.db.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice

@Dao
abstract class MedicalOrderDao : BaseDao<MedicalOrder>() {
    suspend fun getMedicalOrderAndMonitorDeviceListByStatus(status: Int, pageNo: Int, pageSize: Int): List<MedicalOrderAndMonitorDevice>? {
        val limit = pageSize
        val offset = (pageNo - 1) * pageSize
        val query = if (status == 3) {
            SimpleSQLiteQuery(
                "SELECT * FROM MedicalOrder LIMIT ? OFFSET ?",
                arrayOf<Any>(limit, offset)
            )
        } else {
            SimpleSQLiteQuery(
                "SELECT * FROM MedicalOrder WHERE status = ? LIMIT ? OFFSET ?",
                arrayOf<Any>(status, limit, offset)
            )
        }
        return getMedicalOrderAndMonitorDeviceListByStatus(query)
    }

    @Transaction
    @RawQuery
    protected abstract suspend fun getMedicalOrderAndMonitorDeviceListByStatus(query: SupportSQLiteQuery): List<MedicalOrderAndMonitorDevice>?
}