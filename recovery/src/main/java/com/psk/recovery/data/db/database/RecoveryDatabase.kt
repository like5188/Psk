package com.psk.recovery.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.psk.recovery.data.db.dao.MedicalOrderDao
import com.psk.recovery.data.db.dao.MonitorDeviceDao
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        MedicalOrder::class,
        MonitorDevice::class,
    ],
)
abstract class RecoveryDatabase : RoomDatabase() {
    abstract fun medicalOrderDao(): MedicalOrderDao
    abstract fun monitorDeviceDao(): MonitorDeviceDao
}