package com.psk.device.util

import com.psk.device.DeviceManager
import com.psk.device.data.db.DeviceDatabaseManager
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
    //ServerDatabase
    single {
        DeviceDatabaseManager.init(get())
        DeviceDatabaseManager.db
    }
    // DeviceManager
    single {
        DeviceManager(get())
    }

}
