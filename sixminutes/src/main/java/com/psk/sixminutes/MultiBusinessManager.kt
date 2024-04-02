package com.psk.sixminutes

import androidx.activity.ComponentActivity
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.Logger
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType

class MultiBusinessManager {
    val bleHeartRateBusinessManager by lazy {
        BleHeartRateBusinessManager()
    }
    val socketHeartRateBusinessManager by lazy {
        SocketHeartRateBusinessManager()
    }
    val bleBloodOxygenBusinessManager by lazy {
        BleBloodOxygenBusinessManager()
    }
    val bleBloodPressureBusinessManager by lazy {
        BleBloodPressureBusinessManager()
    }

    suspend fun init(
        activity: ComponentActivity,
        devices: List<Info>
    ) {
        if (devices.any { it is BleInfo }) {
            if (!PermissionUtils.requestConnectEnvironment(activity)) {
                return
            }
        }
        DeviceRepositoryManager.init(activity.applicationContext)
        devices.forEach {
            Logger.i(it)
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            bleHeartRateBusinessManager.init(activity.applicationContext, it.name, it.address)
                        }

                        DeviceType.BloodOxygen -> {
                            bleBloodOxygenBusinessManager.init(activity.applicationContext, it.name, it.address)
                        }

                        DeviceType.BloodPressure -> {
                            bleBloodPressureBusinessManager.init(activity.applicationContext, it.name, it.address)
                        }

                        else -> {
                            Logger.e("不支持的设备类型: ${it.deviceType}")
                        }
                    }
                }

                is SocketInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            socketHeartRateBusinessManager.init(it.name, it.hostName, it.port)
                        }

                        else -> {
                            Logger.e("不支持的设备类型: ${it.deviceType}")
                        }
                    }

                }
            }
        }
    }

    fun destroy() {
        bleHeartRateBusinessManager.disconnect()
        socketHeartRateBusinessManager.stop()
        bleBloodOxygenBusinessManager.disconnect()
        bleBloodPressureBusinessManager.disconnect()
    }

}