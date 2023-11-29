package com.psk.device.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psk.device.data.db.dao.BloodOxygenDao
import com.psk.device.data.db.dao.BloodPressureDao
import com.psk.device.data.db.dao.HealthInfoDao
import com.psk.device.data.db.dao.HeartRateDao
import com.psk.device.data.db.dao.OrderDao
import com.psk.device.data.db.dao.ShangXiaZhiDao
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HealthInfo
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.Order
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.util.Converters

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        BloodOxygen::class,
        BloodPressure::class,
        HeartRate::class,
        ShangXiaZhi::class,
        HealthInfo::class,
        Order::class
    ],
)
@TypeConverters(Converters::class)
abstract class DeviceDatabase : RoomDatabase() {
    abstract fun bloodOxygenDao(): BloodOxygenDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun shangXiaZhiDao(): ShangXiaZhiDao
    abstract fun orderDao(): OrderDao
    abstract fun healthInfoDao(): HealthInfoDao
}