package com.psk.shangxiazhi.data.model

import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.ShangXiaZhi
import com.twsz.twsystempre.GameData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.io.Serializable
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

interface IReport : Serializable

/**
 * 上下肢数据报告
 */
class ShangXiaZhiReport : IReport {
    var activeDuration: Int = 0// 主动时长（不包含没有运行的时间）
    var passiveDuration: Int = 0// 被动时长（不包含没有运行的时间）

    var activeMil: Float = 0f// 主动里程
    var passiveMil: Float = 0f// 被动里程

    var activeCal: Float = 0f// 主动卡路里

    var spasm: Int = 0// 痉挛次数
    val spasmLevelList = mutableListOf<Int>()// 所有痉挛等级数据集合
    var spasmLevelTotal: Int = 0 // 总痉挛等级
    var spasmLevelArv: Int = 0// 平均痉挛等级
    var spasmLevelMin: Int = 0// 最小痉挛等级
    var spasmLevelMax: Int = 0// 最大痉挛等级

    val resistanceList = mutableListOf<Int>()// 所有阻力数据集合
    var resistanceTotal: Int = 0 // 总阻力
    var resistanceArv: Int = 0// 平均阻力
    var resistanceMin: Int = 0// 最小阻力
    var resistanceMax: Int = 0// 最大阻力

    val powerList = mutableListOf<Int>()// 所有功率数据集合
    var powerTotal: Int = 0// 总功率
    var powerArv: Int = 0// 平均功率
    var powerMin: Int = 0// 最小功率
    var powerMax: Int = 0// 最大功率

    val speedList = mutableListOf<Int>()// 所有转速数据集合
    var speedTotal: Int = 0// 总转速
    var speedArv: Int = 0// 平均转速
    var speedMin: Int = 0// 最小转速
    var speedMax: Int = 0// 最大转速

    companion object {
        lateinit var report: ShangXiaZhiReport
        private val decimalFormat = DecimalFormat("######0.00")
        private val decimalFormat1 = DecimalFormat("00")

        fun createForm(flow: Flow<ShangXiaZhi>): Flow<GameData> {
            report = ShangXiaZhiReport()
            var isFirstSpasm = false// 是否第一次痉挛
            var mFirstSpasm = 0// 第一次痉挛次数（因为上下肢关机之前的痉挛次数是累计的）
            val gameData = GameData()
            // 这里不能用 distinctUntilChanged、conflate 等操作符，因为需要根据所有数据来计算里程等。必须得到每次数据。
            return flow.buffer(Int.MAX_VALUE).map { shangXiaZhi ->
                if (shangXiaZhi.speed > 0) {
                    /*
                      GameData中的以下字段需要处理：
                        var model: Int = 0,
                        var time: String? = null,
                        var mileage: String? = null,
                        var cal: String? = null,
                        var offset: Int = 0,
                        var offsetValue: Int = 0,
                        var spasm: Int = 0,
                        var spasmFlag: Int = 0,
                      以下字段直接从shangXiaZhi获取值：
                        var speed: Int = 0,
                        var speedLevel: Int = 0,
                        var spasmLevel: Int = 0,
                        var resistance: Int = 0,
                     */
                    gameData.speed = shangXiaZhi.speed
                    gameData.speedLevel = shangXiaZhi.speedLevel
                    gameData.spasmLevel = shangXiaZhi.spasmLevel
                    gameData.resistance = shangXiaZhi.resistance
                    // 速度
                    report.speedList.add(shangXiaZhi.speed)
                    report.speedTotal += shangXiaZhi.speed
                    report.speedArv = report.speedTotal / report.speedList.size
                    report.speedMin = if (report.speedMin <= 0) {
                        shangXiaZhi.speed
                    } else {
                        min(report.speedMin, shangXiaZhi.speed)
                    }
                    report.speedMax = max(report.speedMax, shangXiaZhi.speed)

                    if (shangXiaZhi.model.toInt() == 0x01) {// 被动模式
                        gameData.model = 1// 转换成游戏需要的 0：主动；1：被动
                        report.passiveDuration++// 这里因为上下肢发送数据频率是1秒1条，所以直接以数据量替代时间
                        //被动里程
                        report.passiveMil += shangXiaZhi.speed * 0.5f * 1000 / 3600
                        // 功率，这里添加功率是为了在折线图中显示主动被动的区域
                        report.powerList.add(0)
                        //痉挛。注意：这里不直接使用 ShangXiaZhi 中的 spasm，是因为只要上下肢康复机不关机，那么它返回的痉挛次数值是一直累计的。
                        if (!isFirstSpasm) {
                            isFirstSpasm = true
                            mFirstSpasm = shangXiaZhi.spasm
                        }
                        if (shangXiaZhi.spasm - mFirstSpasm > report.spasm) {
                            report.spasm = shangXiaZhi.spasm - mFirstSpasm
                            gameData.spasmFlag = 1
                        } else {
                            gameData.spasmFlag = 0
                        }
                        report.spasmLevelList.add(shangXiaZhi.spasmLevel)
                        report.spasmLevelTotal += shangXiaZhi.spasmLevel
                        report.spasmLevelArv = report.spasmLevelTotal / report.spasmLevelList.size
                        if (shangXiaZhi.spasmLevel > 0) {
                            report.spasmLevelMin = if (report.spasmLevelMin <= 0) {
                                shangXiaZhi.spasmLevel
                            } else {
                                min(report.spasmLevelMin, shangXiaZhi.spasmLevel)
                            }
                        }
                        report.spasmLevelMax = max(report.spasmLevelMax, shangXiaZhi.spasmLevel)
                        gameData.spasm = report.spasm
                    } else {// 主动模式
                        gameData.model = 0
                        report.activeDuration++
                        //主动里程
                        report.activeMil += shangXiaZhi.speed * 0.5f * 1000 / 3600
                        //卡路里
                        report.activeCal += shangXiaZhi.speed * 0.2f * (shangXiaZhi.resistance * 1.00f / 3.0f) / 60
                        gameData.cal = decimalFormat.format(report.activeCal)
                        // 阻力
                        report.resistanceList.add(shangXiaZhi.resistance)
                        report.resistanceTotal += shangXiaZhi.resistance
                        report.resistanceArv = report.resistanceTotal / report.resistanceList.size
                        if (shangXiaZhi.resistance > 0) {
                            report.resistanceMin = if (report.resistanceMin <= 0) {
                                shangXiaZhi.resistance
                            } else {
                                min(report.resistanceMin, shangXiaZhi.resistance)
                            }
                        }
                        report.resistanceMax = max(report.resistanceMax, shangXiaZhi.resistance)
                        // 功率
                        val power = ((shangXiaZhi.resistance + 3) * shangXiaZhi.speed * 0.134).toInt()
                        report.powerList.add(power)
                        report.powerTotal += power
                        report.powerArv = report.powerTotal / report.powerList.size
                        if (power > 0) {
                            report.powerMin = if (report.powerMin <= 0) {
                                power
                            } else {
                                min(report.powerMin, power)
                            }
                        }
                        report.powerMax = max(report.powerMax, power)
                        //偏差值：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
                        gameData.offset = shangXiaZhi.offset - 15// 转换成游戏需要的 负数：左；0：不偏移；正数：右；
                        // 转换成游戏需要的左边百分比 100~0
                        gameData.offsetValue = 100 - shangXiaZhi.offset * 100 / 30
                    }
                    gameData.mileage = decimalFormat.format(report.activeMil + report.passiveMil)
                    // 时间
                    gameData.time = formatTime(report.activeDuration + report.passiveDuration)
                } else {
                    // 主动模式，速度为0。被动模式不会有速度为0的情况
                    // 此时数据的offset值如果和前一条速度不为0的数据的offset值不相同，就不能使游戏界面停下来。
                    // 这是unity游戏的bug。这里只有通过更改数据来处理了。所以这里把前一次的数据修改速度和模式后发出，也能保证游戏界面停下来时显示的是最近一次速度不为0时的数据，类似被动模式暂停的效果。
                    gameData.model = 0
                    gameData.speed = 0
                }
                gameData
            }
        }

        private fun formatTime(time: Int): String {
            if (time == 0) {
                return "0:00:00"
            }
            val hour = time / 3600
            val minute = time % 3600 / 60
            val second = time % 60
            return "$hour:${decimalFormat1.format(minute)}:${decimalFormat1.format(second)}"
        }
    }

}

/**
 * 心率数据报告
 */
class HeartRateReport : IReport {
    val list = mutableListOf<Int>()// 所有心率数据集合
    var total: Int = 0// 总心率
    var arv: Int = 0// 平均心率
    var min: Int = 0// 最小心率
    var max: Int = 0// 最大心率

    companion object {
        lateinit var report: HeartRateReport
        suspend fun createForm(flow: Flow<HeartRate>) {
            report = HeartRateReport()
            flow.filterNotNull().map {
                it.value
            }.filter {
                it > 0
            }.collect {
                report.list.add(it)
                report.total += it
                report.arv = report.total / report.list.size
                report.min = if (report.min <= 0) {
                    it
                } else {
                    min(report.min, it)
                }
                report.max = max(report.max, it)
            }
        }
    }
}

/**
 * 血氧数据报告
 */
class BloodOxygenReport : IReport {
    var value: Int = 0// 血氧

    companion object {
        lateinit var report: BloodOxygenReport
        suspend fun createForm(flow: Flow<BloodOxygen>) {
            report = BloodOxygenReport()
            flow.distinctUntilChanged().conflate().collect { value ->
                report.value = value.value
            }
        }
    }
}

/**
 * 血压数据报告
 */
class BloodPressureReport : IReport {
    var sbp: Int = 0// 收缩压
    var dbp: Int = 0// 舒张压

    companion object {
        lateinit var report: BloodPressureReport
        suspend fun createForm(flow: Flow<BloodPressure>) {
            report = BloodPressureReport()
            flow.distinctUntilChanged().conflate().collect { value ->
                report.sbp = value.sbp
                report.dbp = value.dbp
            }
        }
    }
}
