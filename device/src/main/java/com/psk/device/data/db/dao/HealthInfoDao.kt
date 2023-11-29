package com.psk.device.data.db.dao

import androidx.room.Dao
import com.psk.device.data.model.HealthInfo

@Dao
abstract class HealthInfoDao : BaseDao<HealthInfo>() {

    /**
     * 根据orderId来判断。如果存在数据则更新，不存在数据则插入。
     */
    suspend fun insertOrUpdate(data: HealthInfo) {
        val oldHealthInfo = getByOrderId(data.orderId)?.firstOrNull()
        if (oldHealthInfo == null) {
            insert(data)
        } else {
            val dataWithId = data.copy(id = oldHealthInfo.id)
            update(dataWithId)
        }
    }
}