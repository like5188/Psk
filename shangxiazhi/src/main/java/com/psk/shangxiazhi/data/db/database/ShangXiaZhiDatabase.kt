package com.psk.shangxiazhi.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psk.device.util.Converters
import com.psk.shangxiazhi.data.db.dao.HealthInfoDao
import com.psk.shangxiazhi.data.db.dao.OrderInfoDao
import com.psk.shangxiazhi.data.model.HealthInfo
import com.psk.shangxiazhi.data.model.OrderInfo

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        HealthInfo::class,
        OrderInfo::class
    ],
)
@TypeConverters(Converters::class)
abstract class ShangXiaZhiDatabase : RoomDatabase() {
    abstract fun orderInfoDao(): OrderInfoDao
    abstract fun healthInfoDao(): HealthInfoDao
}