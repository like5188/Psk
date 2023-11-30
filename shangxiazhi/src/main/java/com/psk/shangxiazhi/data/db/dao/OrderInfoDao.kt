package com.psk.shangxiazhi.data.db.dao

import androidx.room.Dao
import com.psk.device.data.db.dao.BaseDao
import com.psk.shangxiazhi.data.model.OrderInfo

@Dao
abstract class OrderInfoDao : BaseDao<OrderInfo>()