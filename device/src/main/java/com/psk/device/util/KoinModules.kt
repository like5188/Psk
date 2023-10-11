package com.psk.device.util

import com.psk.device.data.model.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.dao.BaseDao
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.HealthInfoRepository
import com.psk.device.data.source.UnionRepository
import com.psk.device.data.source.local.db.DbDataSourceFactory
import com.psk.device.data.source.local.db.HealthInfoDbDataSource
import com.psk.device.data.source.local.db.UnionDbDataSource
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
        val packageName = BaseDao::class.java.`package`?.name
        val paramsClass = Class.forName("$packageName.${deviceType.name}Dao")
        DbDataSourceFactory.create(deviceType, method.invoke(get<DeviceDatabase>()), paramsClass)
    }
    //BaseRemoteDeviceDataSource
    factory { (name: String, deviceType: DeviceType) ->
        BleDataSourceFactory.create(name, deviceType)
    }
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
