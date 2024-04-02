package com.psk.device.data.model

import java.io.Serializable

/**
 * 设备类型
 */
enum class DeviceType(val des: String) : Serializable {
    BloodOxygen("血氧仪"), BloodPressure("血压计"), HeartRate("心电仪"), ShangXiaZhi("上下肢");
}