package com.psk.device.data.source.remote.ble

import android.content.Context
import com.psk.device.DeviceType
import com.psk.device.data.source.remote.BaseRemoteDeviceDataSource
import com.psk.device.util.getSubclasses

/**
 * 蓝牙设备数据源工厂
 */
internal object BleDataSourceFactory {
    private lateinit var dataSourceClasses: List<Class<BaseRemoteDeviceDataSource>>

    suspend fun init(context: Context) {
        if (BleDataSourceFactory::dataSourceClasses.isInitialized) {
            return
        }
        dataSourceClasses = BaseRemoteDeviceDataSource::class.java.getSubclasses(
            context,
            BleDataSourceFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (prefix: String, deviceTypeName: String, Class<BaseRemoteDeviceDataSource>) -> Unit) {
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

}
