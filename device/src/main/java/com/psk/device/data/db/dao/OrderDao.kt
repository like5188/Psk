package com.psk.device.data.db.dao

import androidx.room.Dao
import com.psk.device.data.model.Order

@Dao
abstract class OrderDao : BaseDao<Order>()