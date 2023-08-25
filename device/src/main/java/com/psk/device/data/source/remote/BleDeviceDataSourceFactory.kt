package com.psk.device.data.source.remote

import com.psk.device.DeviceType
import com.psk.device.data.source.remote.BleDeviceDataSourceFactory.dataSources
import com.psk.device.data.source.remote.ble.BP_BloodPressureDataSource
import com.psk.device.data.source.remote.ble.ER1_HeartRateDataSource
import com.psk.device.data.source.remote.ble.O2_BloodOxygenDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.SCI311W_HeartRateDataSource
import com.psk.device.data.source.remote.ble.SCI411C_HeartRateDataSource

/**
 * 蓝牙设备数据源工厂
 * 注意：如果要添加新的蓝牙设备，那么需要一下步骤：
 * 1、新增一个 datasource 到 [com.psk.device.data.source.remote.ble] 中。
 * 2、在[com.psk.device.data.source.DeviceRepository]中添加自己想要的方法。
 * 3、在本类的[dataSources]中增加蓝牙设备的数据源及它名称的前缀。
 */
object BleDeviceDataSourceFactory {
    private val dataSources = mapOf(
        DeviceType.BloodOxygen to mapOf(
            "O2" to O2_BloodOxygenDataSource::class.java
        ),
        DeviceType.BloodPressure to mapOf(
            "BP" to BP_BloodPressureDataSource::class.java
        ),
        DeviceType.HeartRate to mapOf(
            "ER1" to ER1_HeartRateDataSource::class.java,
            "A00219" to SCI311W_HeartRateDataSource::class.java,
            "C00228" to SCI411C_HeartRateDataSource::class.java,
        ),
        DeviceType.ShangXiaZhi to mapOf(
            "RKF" to RKF_ShangXiaZhiDataSource::class.java
        ),
    )

    /**
     * 根据设备名称和设备类型反射创建数据源
     */
    fun create(name: String, deviceType: DeviceType): BaseRemoteDeviceDataSource? {
        val map = dataSources.getOrDefault(deviceType, null) ?: return null
        val key = map.keys.find { name.startsWith(it) }
        if (key.isNullOrEmpty()) {
            return null
        }
        return try {
            map[key]?.newInstance()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 判断指定设备名称[name]是否属于指定设备类型[deviceType]
     */
    fun match(name: String, deviceType: DeviceType): Boolean {
        if (name.isEmpty()) {
            return false
        }
        val map = dataSources.getOrDefault(deviceType, null) ?: return false
        return map.keys.any { name.startsWith(it) }
    }

}
