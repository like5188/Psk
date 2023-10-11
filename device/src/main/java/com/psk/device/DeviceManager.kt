package com.psk.device

import android.content.Context
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HealthInfoRepository
import com.psk.device.data.source.IRepository
import com.psk.device.data.source.UnionRepository
import com.psk.device.data.source.remote.ble.BleDataSourceFactory

/**
 * 设备管理。使用本 [device] 模块时，只需使用此工具类。
 * 1、通过 koin 依赖注入本工具类。
 * 2、调用[init]进行初始化
 * 3、可以直接使用[unionRepository]、[healthInfoRepository]仓库。
 * 4、也可以使用[createRepository]创建设备仓库。
 */
class DeviceManager(
    private val context: Context,
    val unionRepository: UnionRepository,
    val healthInfoRepository: HealthInfoRepository,
) {
    private val repositories = mutableMapOf<DeviceType, IRepository<*>>()


    suspend fun init() {
        // [BleDataSourceFactory]必须放在扫描之前初始化，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
        BleDataSourceFactory.init(context)
    }

    /**
     * 根据设备类型创建仓库（因为[DeviceManager]是single，所以它也是单例
     */
    fun <T : IRepository<*>> createRepository(deviceType: DeviceType): T {
        return if (repositories.containsKey(deviceType)) {
            repositories[deviceType]
        } else {
            // 根据设备类型反射创建仓库
            val className = "${IRepository::class.java.`package`?.name}.${deviceType.name}Repository"
            val clazz = Class.forName(className)
            (clazz.newInstance() as IRepository<*>).apply {
                repositories[deviceType] = this
            }
        } as T
    }

}
