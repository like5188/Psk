package com.psk.device.data.source.remote

import com.psk.device.DeviceType
import com.psk.device.data.source.remote.BleDeviceDataSourceFactory.create
import com.psk.device.data.source.remote.BleDeviceDataSourceFactory.match
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
 * 2、处理本类，包括：添加名称前缀、以及修改[create]、[match]方法。
 * 3、在[com.psk.device.data.source.DeviceRepository]中添加自己想要的方法。
 */
object BleDeviceDataSourceFactory {
    private const val BP_NamePrefix = "BP"
    private const val ER1_NamePrefix = "ER1"
    private const val O2_NamePrefix = "O2"
    private const val RKF_NamePrefix = "RKF"
    private const val SCI311W_NamePrefix = "A00219"
    private const val SCI411C_NamePrefix = "C00228"

    /**
     * 根据设备名称和设备类型创建数据源
     */
    fun create(name: String, deviceType: DeviceType): BaseRemoteDeviceDataSource? {
        return when (deviceType) {
            DeviceType.BloodOxygen -> {
                when {
                    name.startsWith(O2_NamePrefix) -> {
                        O2_BloodOxygenDataSource()
                    }

                    else -> null
                }
            }

            DeviceType.BloodPressure -> {
                when {
                    name.startsWith(BP_NamePrefix) -> {
                        BP_BloodPressureDataSource()
                    }

                    else -> null
                }
            }

            DeviceType.HeartRate -> {
                when {
                    name.startsWith(ER1_NamePrefix) -> {
                        ER1_HeartRateDataSource()
                    }

                    name.startsWith(SCI311W_NamePrefix) -> {
                        SCI311W_HeartRateDataSource()
                    }

                    name.startsWith(SCI411C_NamePrefix) -> {
                        SCI411C_HeartRateDataSource()
                    }

                    else -> null
                }
            }

            DeviceType.ShangXiaZhi -> {
                when {
                    name.startsWith(RKF_NamePrefix) -> {
                        RKF_ShangXiaZhiDataSource()
                    }

                    else -> null
                }
            }
        }
    }

    /**
     * 当前设备名称和设备类型是否匹配
     */
    fun match(name: String, deviceType: DeviceType): Boolean {
        if (name.isEmpty()) {
            return false
        }
        return when (deviceType) {
            DeviceType.BloodOxygen -> name.startsWith(O2_NamePrefix)
            DeviceType.BloodPressure -> name.startsWith(BP_NamePrefix)
            DeviceType.HeartRate -> name.startsWith(ER1_NamePrefix) ||
                    name.startsWith(SCI311W_NamePrefix) ||
                    name.startsWith(SCI411C_NamePrefix)

            DeviceType.ShangXiaZhi -> name.startsWith(RKF_NamePrefix)
        }
    }
}