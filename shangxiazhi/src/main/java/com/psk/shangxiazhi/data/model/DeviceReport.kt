package com.psk.shangxiazhi.data.model

import java.io.Serializable

// 上下肢计算的汇总数据
class DeviceReport : Serializable {
    val heartRateList = mutableListOf<Int>()// 所有心率数据集合
    var heartRateTotal: Int = 0// 总心率
    var heartRateArv: Int = 0// 平均心率
    var heartRateMin: Int = -1// 最小心率
    var heartRateMax: Int = 0// 最大心率

    var bloodOxygen: Int = 0// 血氧
    var bloodPressureSbp: Int = 0// 收缩压
    var bloodPressureDbp: Int = 0// 舒张压

}
