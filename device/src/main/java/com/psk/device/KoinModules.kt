package com.psk.device

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.dao.BaseDao
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.RepositoryFactory
import com.psk.device.data.source.local.db.DbDataSourceFactory
import com.psk.device.data.source.remote.ble.BleDataSourceFactory
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
    //IDbDataSource
    factory { (deviceType: DeviceType) ->
        // 从DeviceDatabase中获取deviceType对应的方法
        val method = DeviceDatabase::class.java.declaredMethods.firstOrNull {
            it.name.lowercase().startsWith(deviceType.name.lowercase())
        } ?: return@factory null
        method.isAccessible = true
        DbDataSourceFactory.create(deviceType, method.invoke(get<DeviceDatabase>()) as BaseDao<*>)
    }
    //BaseRemoteDeviceDataSource
    factory { (name: String, deviceType: DeviceType) ->
        BleDataSourceFactory.create(name, deviceType)
    }

    //IRepository
    factory { (deviceType: DeviceType) ->
        RepositoryFactory.create(deviceType)
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

}
