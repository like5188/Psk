package com.psk.device.util

import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.remote.BleDataSourceFactory

/**
 * 判断当前设备类型是否包含指定设备名称[name]的设备
 */
fun DeviceType.containsDevice(name: String): Boolean {
    if (name.isEmpty()) {
        return false
    }
    BleDataSourceFactory.foreach { prefix, deviceTypeName, clazz ->
        if (deviceTypeName == this.name && name.startsWith(prefix)) {
            return true
        }
    }
    return false
}
