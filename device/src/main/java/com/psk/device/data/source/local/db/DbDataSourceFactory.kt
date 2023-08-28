package com.psk.device.data.source.local.db

import android.content.Context
import com.psk.ble.DeviceType
import com.psk.device.data.source.local.IDbDataSource
import com.psk.device.util.getSubclasses

/**
 * 数据库数据源工厂
 */
internal object DbDataSourceFactory {
    private lateinit var classes: List<Class<IDbDataSource<*>>>

    suspend fun init(context: Context) {
        if (::classes.isInitialized) {
            return
        }
        classes = IDbDataSource::class.java.getSubclasses(
            context,
            DbDataSourceFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (deviceTypeName: String, Class<IDbDataSource<*>>) -> Unit) {
        for (clazz in classes) {
            val deviceTypeName = clazz.simpleName.replace("DbDataSource", "")
            block(deviceTypeName, clazz)
        }
    }

    /**
     * 根据设备类型反射创建数据源
     */
    fun create(deviceType: DeviceType, dao: Any?, paramsClass: Class<*>): IDbDataSource<*>? {
        foreach { deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name) {
                return try {
                    val constructor = clazz.getConstructor(paramsClass)
                    constructor.isAccessible = true
                    constructor.newInstance(dao)
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

}
