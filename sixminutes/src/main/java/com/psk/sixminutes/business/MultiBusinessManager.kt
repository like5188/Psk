package com.psk.sixminutes.business

import androidx.activity.ComponentActivity
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.Logger
import com.psk.device.DeviceRepositoryManager
import com.psk.device.ScanManager
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.business.ble.BleBloodOxygenBusinessManager
import com.psk.sixminutes.business.ble.BleBloodPressureBusinessManager
import com.psk.sixminutes.business.ble.BleHeartRateBusinessManager
import com.psk.sixminutes.business.socket.SocketHeartRateBusinessManager
import com.psk.sixminutes.data.model.BleInfo
import com.psk.sixminutes.data.model.Info
import com.psk.sixminutes.data.model.SocketInfo

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

    suspend fun init(activity: ComponentActivity, devices: List<Info>?) {
        if (devices.isNullOrEmpty()) {
            return
        }
        val hasBleInfo = devices.any { it is BleInfo }
        // 对于6分钟的机器，连接血氧需要先扫描，然后才能连接。
        val hasBloodOxygen = devices.any { it is BleInfo && it.deviceType == DeviceType.BloodOxygen }
        if (hasBleInfo) {
            if (hasBloodOxygen) {
                if (!PermissionUtils.requestScanEnvironment(activity)) {
                    return
                }
            } else {
                if (!PermissionUtils.requestConnectEnvironment(activity)) {
                    return
                }
            }
        }
        DeviceRepositoryManager.init(activity.applicationContext)
        if (hasBloodOxygen) {
            ScanManager.init(activity.applicationContext)
        }
        devices.forEach {
            Logger.i(it)
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            bleHeartRateBusinessManager.init(activity, it.name, it.address)
                        }

                        DeviceType.BloodOxygen -> {
                            bleBloodOxygenBusinessManager.init(activity, it.name, it.address)
                        }

                        DeviceType.BloodPressure -> {
                            bleBloodPressureBusinessManager.init(activity, it.name, it.address)
                        }

                        else -> {
                            Logger.e("不支持的设备类型: ${it.deviceType}")
                        }
                    }
                }

                is SocketInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            socketHeartRateBusinessManager.init(activity, it.name, it.hostName, it.port)
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