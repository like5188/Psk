package com.psk.device

import android.content.Context
import com.psk.ble.DeviceType
import com.psk.device.data.source.local.db.DbDataSourceFactory
import com.psk.device.data.source.remote.ble.BleDataSourceFactory
import com.psk.device.util.deviceModule
import org.koin.core.context.loadKoinModules

/**
 * 设备管理。使用本模块时，只需使用此工具类进行初始化，然后使用koin注入仓库来使用。
 * 注意：
 * 一、如果要添加新的蓝牙设备系列（非当前已经存在的系列：血压、血氧、心电、上下肢），那么需要以下步骤：
 * 1、新增对应实体类放到[com.psk.device.data.model]中。
 * 2、新增一个对应的Dao。
 * 3、在[com.psk.device.data.db.database.DeviceDatabase]类中新增相关方法和entities。
 * 4、新增一个DbDataSource。名称格式为：[DeviceType]DbDataSource；包名为：[com.psk.device.data.source.local.db]。
 * 5、新增一个BaseDataSource。名称格式为：Base[DeviceType]Datasource；包名为：[com.psk.device.data.source.remote]。
 * 6、新增一个DataSource。名称格式为：[扫描出来的蓝牙设备的名称前缀]_[DeviceType]Datasource；包名为：[com.psk.device.data.source.remote.ble]。
 * 7、新增一个Repository。名称格式为：[DeviceType]Repository；包名为：[com.psk.device.data.source]。
 *
 * 二、如果只是要添加新的蓝牙设备，那么需要以下步骤：
 * 1、新增一个DataSource。名称格式为：[扫描出来的蓝牙设备的名称前缀]_[DeviceType]Datasource；包名为：[com.psk.device.data.source.remote.ble]。
 */
object DeviceManager {
    suspend fun init(context: Context) {
        loadKoinModules(deviceModule)
        // [BleDataSourceFactory]必须放在扫描之前初始化，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
        BleDataSourceFactory.init(context)
        DbDataSourceFactory.init(context)
    }
}