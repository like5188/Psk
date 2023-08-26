package com.psk.device.data.source

import android.content.Context
import com.psk.ble.DeviceType
import com.psk.device.util.getSubclasses

/**
 * 仓库工厂
 */
internal object RepositoryFactory {
    private lateinit var classes: List<Class<IRepository<*>>>

    suspend fun init(context: Context) {
        if (::classes.isInitialized) {
            return
        }
        classes = IRepository::class.java.getSubclasses(
            context,
            RepositoryFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (deviceTypeName: String, Class<IRepository<*>>) -> Unit) {
        for (clazz in classes) {
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
