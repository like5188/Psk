package com.psk.device.util

import com.psk.device.DeviceManager
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.HealthInfoRepository
import com.psk.device.data.source.UnionRepository
import com.psk.device.data.source.local.db.HealthInfoDbDataSource
import com.psk.device.data.source.local.db.UnionDbDataSource
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
    //UnionDbDataSource
    factory {
        UnionDbDataSource(get<DeviceDatabase>().unionDao())
    }
    //UnionRepository
    factory {
        UnionRepository(get())
    }
    //HealthInfoDbDataSource
    factory {
        HealthInfoDbDataSource(get<DeviceDatabase>().healthInfoDao())
    }
    //HealthInfoRepository
    factory {
        HealthInfoRepository(get())
    }

    //ServerDatabase
    single {
        DeviceDatabaseManager.init(get())
        DeviceDatabaseManager.db
    }
    // DeviceManager
    single {
        DeviceManager(get(), get(), get())
    }

}
