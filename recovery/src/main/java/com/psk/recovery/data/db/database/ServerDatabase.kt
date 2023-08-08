package com.psk.recovery.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.psk.recovery.data.db.dao.BloodOxygenDao
import com.psk.recovery.data.db.dao.BloodPressureDao
import com.psk.recovery.data.db.dao.HeartRateDao
import com.psk.recovery.data.db.dao.MedicalOrderDao
import com.psk.recovery.data.db.dao.MonitorDeviceDao
import com.psk.recovery.data.db.dao.ShangXiaZhiDao
import com.psk.recovery.data.model.BloodOxygen
import com.psk.recovery.data.model.BloodPressure
import com.psk.recovery.data.model.HeartRate
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.util.Converters

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        BloodOxygen::class,
        BloodPressure::class,
        HeartRate::class,
        MedicalOrder::class,
        MonitorDevice::class,
        ShangXiaZhi::class
    ],
)
@TypeConverters(Converters::class)
abstract class ServerDatabase : RoomDatabase() {
    abstract fun bloodOxygenDao(): BloodOxygenDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun medicalOrderDao(): MedicalOrderDao
    abstract fun monitorDeviceDao(): MonitorDeviceDao
    abstract fun shangXiaZhiDao(): ShangXiaZhiDao
}