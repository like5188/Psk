package com.psk.device

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.DeviceRepository
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.BaseBloodOxygenDataSource
import com.psk.device.data.source.remote.BaseBloodPressureDataSource
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import com.psk.device.data.source.remote.BaseShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.BP_BloodPressureDataSource
import com.psk.device.data.source.remote.ble.ER1_HeartRateDataSource
import com.psk.device.data.source.remote.ble.O2_BloodOxygenDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.SCI311W_HeartRateDataSource
import com.psk.device.data.source.remote.ble.SCI411C_HeartRateDataSource
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
    factory<BaseBloodOxygenDataSource>(named(O2_BloodOxygenDataSource.NAME_PREFIX)) {
        O2_BloodOxygenDataSource()
    }
    factory<BaseBloodPressureDataSource>(named(BP_BloodPressureDataSource.NAME_PREFIX)) {
        BP_BloodPressureDataSource()
    }
    factory<BaseHeartRateDataSource>(named(SCI311W_HeartRateDataSource.NAME_PREFIX)) {
        SCI311W_HeartRateDataSource()
    }
    factory<BaseHeartRateDataSource>(named(ER1_HeartRateDataSource.NAME_PREFIX)) {
        ER1_HeartRateDataSource()
    }
    factory<BaseHeartRateDataSource>(named(SCI411C_HeartRateDataSource.NAME_PREFIX)) {
        SCI411C_HeartRateDataSource()
    }
    factory<BaseShangXiaZhiDataSource>(named(RKF_ShangXiaZhiDataSource.NAME_PREFIX)) {
        RKF_ShangXiaZhiDataSource()
    }

    //repository
    factory {
        DeviceRepository(
            get(),
            get(),
            get(),
            get(),
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
