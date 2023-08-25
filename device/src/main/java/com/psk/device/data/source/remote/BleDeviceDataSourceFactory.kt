package com.psk.device.data.source.remote

import android.content.Context
import com.psk.device.DeviceType
import com.psk.device.util.getSubclasses

/**
 * 蓝牙设备数据源工厂
 */
internal object BleDeviceDataSourceFactory {
    private lateinit var dataSourceClasses: List<Class<out BaseRemoteDeviceDataSource>>

    suspend fun init(context: Context) {
        if (::dataSourceClasses.isInitialized) {
            return
        }
        dataSourceClasses = BaseRemoteDeviceDataSource::class.java.getSubclasses(context, "com.psk.device.data.source.remote.ble")
    }

    private inline fun foreach(block: (prefix: String, deviceTypeName: String, Class<out BaseRemoteDeviceDataSource>) -> Unit) {
        for (clazz in dataSourceClasses) {
            val split = clazz.simpleName.split("_")
            if (split.size != 2) {
                continue
            }
            val prefix = split[0]
            val deviceTypeName = split[1].replace("DataSource", "")
            block(prefix, deviceTypeName, clazz)
        }
    }

    /**
     * 根据设备名称和设备类型反射创建数据源
     */
    fun create(name: String, deviceType: DeviceType): BaseRemoteDeviceDataSource? {
        foreach { prefix, deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name && name.startsWith(prefix)) {
                return try {
                    clazz.newInstance()
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    /**
     * 判断指定设备名称[name]是否属于指定设备类型[deviceType]
     */
    fun match(name: String, deviceType: DeviceType): Boolean {
        if (name.isEmpty()) {
            return false
        }
        foreach { prefix, deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name && name.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

}
