package com.psk.device.data.db.dao

import androidx.room.Dao
import com.psk.device.data.model.OrderInfo

@Dao
abstract class OrderInfoDao : BaseDao<OrderInfo>()