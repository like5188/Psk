package com.psk.device.data.source.local.db

import com.psk.device.data.model.DeviceType

/**
 * 数据库数据源工厂
 */
internal object DbDataSourceFactory {
    /**
     * 根据设备类型反射创建数据源
     */
    fun <T> create(deviceType: DeviceType, dao: Any?, paramsClass: Class<*>): IDbDataSource<T> {
        val className = "${DbDataSourceFactory::class.java.`package`?.name}.${deviceType.name}DbDataSource"
        val clazz = Class.forName(className)
        val constructor = clazz.getConstructor(paramsClass)
        constructor.isAccessible = true
        return constructor.newInstance(dao) as IDbDataSource<T>
    }

}
