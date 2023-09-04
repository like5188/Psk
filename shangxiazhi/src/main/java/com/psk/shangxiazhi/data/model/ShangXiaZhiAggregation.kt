package com.psk.shangxiazhi.data.model

import java.io.Serializable

// 上下肢计算的汇总数据
class ShangXiaZhiAggregation : Serializable {
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

    var speedTotal: Int = 0// 总转速
    var speedArv: Int = 0// 平均转速
    var speedMin: Int = 0// 最小转速
    var speedMax: Int = 0// 最大转速
    val speedList = mutableListOf<Int>()// 所有转速数据集合
}
