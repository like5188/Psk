package com.psk.sixminutes

import androidx.activity.ComponentActivity
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.Logger
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType

class MultiBusinessManager {
    val heartRateBusinessManager by lazy {
        HeartRateBusinessManager()
    }
    val bloodOxygenBusinessManager by lazy {
        BloodOxygenBusinessManager()
    }
    val bloodPressureBusinessManager by lazy {
        BloodPressureBusinessManager()
    }

    /**
     * @param devices   设备信息。key:设备类型，value:设备名称和地址。
     */
    suspend fun init(
        activity: ComponentActivity,
        devices: Map<DeviceType, Pair<String, String>>
    ) {
        if (!PermissionUtils.requestConnectEnvironment(activity)) {
            return
        }
        DeviceRepositoryManager.init(activity.applicationContext)
        devices.forEach {
            val deviceType = it.key
            val name = it.value.first
            val address = it.value.second
            Logger.i("deviceType=$deviceType, name=$name, address=$address")
            when (deviceType) {
                DeviceType.HeartRate -> {
                    heartRateBusinessManager.init(activity.applicationContext, name, address)
                }

                DeviceType.BloodOxygen -> {
                    bloodOxygenBusinessManager.init(activity.applicationContext, name, address)
                }

                DeviceType.BloodPressure -> {
                    bloodPressureBusinessManager.init(activity.applicationContext, name, address)
                }

                else -> {
                    Logger.e("不支持的设备类型: $deviceType")
                }
            }
        }
    }

    fun destroy() {
        heartRateBusinessManager.disconnect()
        bloodOxygenBusinessManager.disconnect()
        bloodPressureBusinessManager.disconnect()
    }

}