package com.psk.sixminutes.data.db.dao

import androidx.room.Dao
import com.psk.common.db.BaseDao
import com.psk.sixminutes.data.model.HealthInfo

@Dao
abstract class HealthInfoDao : BaseDao<HealthInfo>()