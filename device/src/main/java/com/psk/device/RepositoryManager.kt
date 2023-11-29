package com.psk.device

import androidx.activity.ComponentActivity
import com.like.ble.util.PermissionUtils
import com.psk.device.RepositoryManager.createBleDeviceRepository
import com.psk.device.RepositoryManager.healthInfoRepository
import com.psk.device.RepositoryManager.init
import com.psk.device.RepositoryManager.orderRepository
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BaseBleDeviceRepository
import com.psk.device.data.source.HealthInfoRepository
import com.psk.device.data.source.OrderRepository
import com.psk.device.data.source.remote.BleDataSourceFactory
import kotlin.collections.set

/**
 * 仓库管理工具类。
 * 1、调用[init]进行初始化
 * 2、可以直接使用[orderRepository]、[healthInfoRepository]仓库。
 * 3、也可以使用[createBleDeviceRepository]创建的设备仓库。
 */
object RepositoryManager {
    private val bleDeviceRepositories = mutableMapOf<DeviceType, BaseBleDeviceRepository<*>>()
    val orderRepository by lazy { OrderRepository() }
    val healthInfoRepository by lazy { HealthInfoRepository() }

    suspend fun init(activity: ComponentActivity) {
        PermissionUtils.requestConnectEnvironment(activity)
        DeviceDatabaseManager.init(activity.applicationContext)
        // [BleDataSourceFactory]必须放在扫描之前初始化，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
        BleDataSourceFactory.init(activity.applicationContext)
    }

    /**
     * 根据蓝牙设备类型创建仓库
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
