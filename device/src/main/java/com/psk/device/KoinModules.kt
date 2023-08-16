package com.psk.device

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.DeviceRepository
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.IBloodOxygenDataSource
import com.psk.device.data.source.remote.IBloodPressureDataSource
import com.psk.device.data.source.remote.IHeartRateDataSource
import com.psk.device.data.source.remote.IShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.BP88B180704_BloodPressureDataSource
import com.psk.device.data.source.remote.ble.ER1_HeartRateDataSource
import com.psk.device.data.source.remote.ble.O2_BloodOxygenDataSource
import com.psk.device.data.source.remote.ble.SCI311W_HeartRateDataSource
import com.psk.device.data.source.remote.ble.XZX_ShangXiaZhiDataSource
import org.koin.core.qualifier.named
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
    factory<IBloodOxygenDataSource>(named("O2")) {
        O2_BloodOxygenDataSource(get())
    }
    factory<IBloodPressureDataSource>(named("BP88B180704")) {
        BP88B180704_BloodPressureDataSource(get())
    }
    factory<IHeartRateDataSource>(named("SCI311W")) {
        SCI311W_HeartRateDataSource(get())
    }
    factory<IHeartRateDataSource>(named("ER1")) {
        ER1_HeartRateDataSource(get())
    }
    factory<IShangXiaZhiDataSource>(named("XZX")) {
        XZX_ShangXiaZhiDataSource(get())
    }

    //repository
    factory {
        DeviceRepository(
            get(),
            get(),
            get(),
            get(),
            get(named("O2")),
            get(named("BP88B180704")),
            get(named("ER1")),
            get(named("XZX")),
            get()
        )
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

    // BleManager
    single {
        BleManager(get())
    }
}
