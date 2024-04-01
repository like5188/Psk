package com.psk.device

import android.content.Context
import com.psk.device.DeviceRepositoryManager.createBleDeviceRepository
import com.psk.device.DeviceRepositoryManager.init
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BaseBleDeviceRepository
import com.psk.device.data.source.remote.BleDataSourceFactory
import com.psk.device.socket.SocketHeartRateRepository
import kotlin.collections.set

/**
 * 仓库管理工具类。
 * 1、调用[init]进行初始化
 * 2、使用[createBleDeviceRepository]创建设备仓库。
 * 3、使用设备仓库中的方法。
 * 注意：调用[BaseBleDeviceRepository.connect]进行连接之前必须请求连接环境[com.like.ble.central.util.PermissionUtils.requestConnectEnvironment]，
 * 但是如果已经请求了扫描环境[com.like.ble.central.util.PermissionUtils.requestScanEnvironment]，那么这里就不需要请求连接环境了，因为扫描环境包含了连接环境。
 */
object DeviceRepositoryManager {
    private val bleDeviceRepositories = mutableMapOf<DeviceType, BaseBleDeviceRepository<*>>()

    suspend fun init(context: Context) {
        DeviceDatabaseManager.init(context.applicationContext)
        // [BleDataSourceFactory]必须放在扫描之前初始化，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
        BleDataSourceFactory.init(context.applicationContext)
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

    fun createSocketHeartRateRepository(): SocketHeartRateRepository {
        return SocketHeartRateRepository()
    }

}
