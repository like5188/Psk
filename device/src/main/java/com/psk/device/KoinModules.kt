package com.psk.device

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.DeviceRepository
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.BleDeviceDataSourceFactory
import org.koin.dsl.module

/**
 * File Name: KoinModules.kt
 * Description: koin 依赖注入的 module
 * Author: like
 * Date: 2023-06-12
 * Modify:
 * Date:
 */
val deviceModule = module {
    //DataSource
    factory {
        BloodOxygenDbDataSource(get())
    }
    factory {
        BloodPressureDbDataSource(get())
    }
    factory {
        HeartRateDbDataSource(get())
    }
    factory {
        ShangXiaZhiDbDataSource(get())
    }
    factory { (name: String, deviceType: DeviceType) ->
        BleDeviceDataSourceFactory.create(name, deviceType)
    }

    //repository
    factory {
        DeviceRepository()
    }

    // BleManager
    single {
        BleManager(get())
    }

    //ServerDatabase
    single {
        DeviceDatabaseManager.init(get())
        DeviceDatabaseManager.db
    }

    // Dao
    single {
        get<DeviceDatabase>().bloodOxygenDao()
    }
    single {
        get<DeviceDatabase>().bloodPressureDao()
    }
    single {
        get<DeviceDatabase>().heartRateDao()
    }
    single {
        get<DeviceDatabase>().shangXiaZhiDao()
    }
}
