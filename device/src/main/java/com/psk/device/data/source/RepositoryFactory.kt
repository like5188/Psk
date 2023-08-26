package com.psk.device.data.source

import android.content.Context
import com.psk.device.DeviceType
import com.psk.device.util.getSubclasses

/**
 * 仓库工厂
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
 *
 */
internal object RepositoryFactory {
    private lateinit var dataSourceClasses: List<Class<IRepository<*>>>

    suspend fun init(context: Context) {
        if (::dataSourceClasses.isInitialized) {
            return
        }
        dataSourceClasses = IRepository::class.java.getSubclasses(
            context,
            RepositoryFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (deviceTypeName: String, Class<IRepository<*>>) -> Unit) {
        for (clazz in dataSourceClasses) {
            val deviceTypeName = clazz.simpleName.replace("Repository", "")
            block(deviceTypeName, clazz)
        }
    }

    /**
     * 根据设备类型反射创建仓库
     */
    fun create(deviceType: DeviceType): IRepository<*>? {
        foreach { deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name) {
                return try {
                    clazz.newInstance()
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

}
