package com.psk.device

import android.content.Context
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BaseBleDeviceRepository
import com.psk.device.data.source.HealthInfoRepository
import com.psk.device.data.source.UnionRepository
import com.psk.device.data.source.remote.BleDataSourceFactory
import kotlin.collections.set

/**
 * 设备管理（单例）。使用本 [device] 模块时，只需使用此工具类。
 * 1、通过 koin 依赖注入本工具类。
 * 2、调用[init]进行初始化
 * 3、可以直接使用[unionRepository]、[healthInfoRepository]仓库。
 * 4、也可以使用[createBleDeviceRepository]创建的设备仓库。
 */
class DeviceManager(private val context: Context) {
    private val bleDeviceRepositories = mutableMapOf<DeviceType, BaseBleDeviceRepository<*>>()
    val unionRepository by lazy { UnionRepository() }
    val healthInfoRepository by lazy { HealthInfoRepository() }

    suspend fun init() {
        // [BleDataSourceFactory]必须放在扫描之前初始化，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
        BleDataSourceFactory.init(context)
    }

    /**
     * 根据蓝牙设备类型创建仓库（因为[DeviceManager]是single，所以它也是单例
     */
    fun <T : BaseBleDeviceRepository<*>> createBleDeviceRepository(deviceType: DeviceType): T {
        return if (bleDeviceRepositories.containsKey(deviceType)) {
            bleDeviceRepositories[deviceType]
        } else {
            // 根据设备类型反射创建仓库
            val className = "${BaseBleDeviceRepository::class.java.`package`?.name}.${deviceType.name}Repository"
            val clazz = Class.forName(className)
            (clazz.newInstance() as BaseBleDeviceRepository<*>).apply {
                bleDeviceRepositories[deviceType] = this
            }
        } as T
    }

}
