package com.psk.device.data.source

import com.psk.device.data.model.DeviceType

/**
 * 仓库工厂
 */
internal object RepositoryFactory {
    /**
     * 根据设备类型反射创建仓库
     */
    fun create(deviceType: DeviceType): IRepository<*> {
        val className = "${RepositoryFactory::class.java.`package`?.name}.${deviceType.name}Repository"
        val clazz = Class.forName(className)
        return clazz.newInstance() as IRepository<*>
    }

}
