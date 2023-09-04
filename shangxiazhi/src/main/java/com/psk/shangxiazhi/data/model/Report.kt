package com.psk.shangxiazhi.data.model

import java.io.Serializable

interface IReport : Serializable

/**
 * 上下肢数据报告
 */
class ShangXiaZhiReport : IReport {
    var count: Int = 0// 总的数据量

    var activeDuration: Int = 0// 主动时长
    var passiveDuration: Int = 0// 被动时长

    var activeMil: Float = 0f// 主动里程
    var passiveMil: Float = 0f// 被动里程

    var activeCal: Float = 0f// 主动卡路里
    var passiveCal: Float = 0f// 被动卡路里

    var spasm: Int = 0// 痉挛次数
    var spasmLevelTotal: Int = 0 // 总痉挛等级
    var spasmLevelArv: Int = 0// 平均痉挛等级
    var spasmLevelMin: Int = -1// 最小痉挛等级
    var spasmLevelMax: Int = 0// 最大痉挛等级

    var resistanceTotal: Int = 0 // 总阻力
    var resistanceArv: Int = 0// 平均阻力
    var resistanceMin: Int = -1// 最小阻力
    var resistanceMax: Int = 0// 最大阻力

    val speedList = mutableListOf<Int>()// 所有转速数据集合
    var speedTotal: Int = 0// 总转速
    var speedArv: Int = 0// 平均转速
    var speedMin: Int = -1// 最小转速
    var speedMax: Int = 0// 最大转速
}

/**
 * 心率数据报告
 */
class HeartRateReport : IReport {
    val list = mutableListOf<Int>()// 所有心率数据集合
    var total: Int = 0// 总心率
    var arv: Int = 0// 平均心率
    var min: Int = -1// 最小心率
    var max: Int = 0// 最大心率
}

/**
 * 血氧数据报告
 */
class BloodOxygenReport : IReport {
    var value: Int = 0// 血氧
}

/**
 * 血压数据报告
 */
class BloodPressureReport : IReport {
    var sbp: Int = 0// 收缩压
    var dbp: Int = 0// 舒张压
}
