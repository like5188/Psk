package com.psk.shangxiazhi.data.model

import java.io.Serializable

// 上下肢计算的汇总数据
class ShangXiaZhiCalcTotal : Serializable {
    var count: Int = 0// 总的数据量

    var activeDuration: Int = 0// 主动时长
    var passiveDuration: Int = 0// 被动时长

    var activeMil: Float = 0f// 主动里程
    var passiveMil: Float = 0f// 被动里程

    var activeCal: Float = 0f// 主动卡路里
    var passiveCal: Float = 0f// 被动卡路里

    var spasm: Int = 0// 痉挛次数

    var resistanceTotal: Int = 0 // 总阻力
    var resistanceArv: Int = 0// 平均阻力
    var resistanceMin: Int = 0// 最小阻力
    var resistanceMax: Int = 0// 最大阻力

    var speedTotal: Int = 0// 总速度
    var speedArv: Int = 0// 平均速度
    var speedMin: Int = 0// 最小速度
    var speedMax: Int = 0// 最大速度
}

// 上下肢计算的当次数据
class ShangXiaZhiCalcCurrent {
    var time: String = ""// 当前时间
    var model: Int = 0// 主被动模式
    var speed: Int = 0// 转速
    var speedLevel: Int = 0// 转速等级
    var resistance: Int = 0//阻力
    var offset: Int = 0// 偏差方向值
    var offsetValue: Int = 0// 偏差值
    var spasmLevel: Int = 0// 痉挛等级
    var spasmFlag: Int = 0// 是否痉挛的标记
}