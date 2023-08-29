package com.psk.device.data.source

import com.psk.ble.DeviceType

/**
 * 仓库工厂
 */
internal object RepositoryFactory {
    /**
     * 根据设备类型反射创建仓库
     */
    fun create(deviceType: DeviceType): IRepository<*> {
        val className = "${RepositoryFactory::class.java.`package`?.name}${deviceType.name}Repository"
        return Class.forName(className) as IRepository<*>
    }

}
