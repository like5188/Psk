package com.psk.device.data.source.remote.ble

import android.content.Context
import com.like.common.util.getSubclasses
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.remote.ble.base.BaseBleDeviceDataSource

/**
 * 蓝牙设备数据源工厂
 */
internal object BleDataSourceFactory {
    private lateinit var classes: List<Class<BaseBleDeviceDataSource>>

    suspend fun init(context: Context) {
        if (BleDataSourceFactory::classes.isInitialized) {
            return
        }
        classes = BaseBleDeviceDataSource::class.java.getSubclasses(
            context, BleDataSourceFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (prefix: String, deviceTypeName: String, Class<BaseBleDeviceDataSource>) -> Unit) {
        for (clazz in classes) {
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
    fun <T : BaseBleDeviceDataSource> create(name: String, deviceType: DeviceType): T {
        foreach { prefix, deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name && name.startsWith(prefix)) {
                return clazz.newInstance() as T
            }
        }
        throw IllegalArgumentException("未找到 $deviceType $name 对应的数据源")
    }

}
