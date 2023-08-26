package com.psk.device.data.source

import android.content.Context
import com.psk.device.DeviceType
import com.psk.device.util.getSubclasses

/**
 * 仓库工厂
 * 注意：如果要添加新的蓝牙设备系列，那么需要以下步骤：
 * 1、新增一个Repository。名称格式为：[DeviceType]Repository；包名为：[com.psk.device.data.source]。
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
