package com.psk.sixminutes.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psk.device.util.Converters
import com.psk.sixminutes.data.db.dao.HealthInfoDao
import com.psk.sixminutes.data.model.HealthInfo

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        HealthInfo::class,
    ],
)
@TypeConverters(Converters::class)
abstract class SixMinutesDatabase : RoomDatabase() {
    abstract fun healthInfoDao(): HealthInfoDao
}