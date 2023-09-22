package com.psk.device.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.twsz.remotecommands.TrunkCommandData

// 上下肢传递过来的数据
@Entity
data class ShangXiaZhi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    var model: Byte = 0,//模式： 0x01 表示  被动 0x02 表示  主动
    val speedLevel: Int = 0,//速度档位：范围0~12           十六进制：0x00~0x3c
    val speed: Int = 0,//速度圈数：范围0~
    val offset: Int = 0,//偏移：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
    val spasm: Int = 0,//痉挛次数：范围0~
    val spasmLevel: Int = 0,//痉挛等级：范围1~12   十六进制：0x01~0x0c
    val resistance: Int = 0,//阻力(被动模式永远返回1)：范围1~12           十六进制：0x01~0x0c
    val intelligence: Byte = 0,//智能： 0x40 表示 关闭 0x41 表示 打开
    val direction: Byte = 0,//正反转： 0x50 表示 反转 0x51 表示 正转
    val medicalOrderId: Long = 0,
) {
    @Ignore
    var curTime: String = ""//当前倒计时。用于游戏界面显示。其实上下肢没有返回这个数据，是自己计算的。
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShangXiaZhi) return false

        if (model != other.model) return false
        if (speedLevel != other.speedLevel) return false
        if (speed != other.speed) return false
        if (offset != other.offset) return false
        if (spasm != other.spasm) return false
        if (spasmLevel != other.spasmLevel) return false
        if (resistance != other.resistance) return false
        if (intelligence != other.intelligence) return false
        if (direction != other.direction) return false
        if (curTime != other.curTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.toInt()
        result = 31 * result + speedLevel
        result = 31 * result + speed
        result = 31 * result + offset
        result = 31 * result + spasm
        result = 31 * result + spasmLevel
        result = 31 * result + resistance
        result = 31 * result + intelligence
        result = 31 * result + direction
        result = 31 * result + curTime.hashCode()
        return result
    }
}

/**
 * 初始化上下肢时需要的参数，设置好后，如果是被动模式，上下肢会自动运行
 *
 * @param passiveModule     被动模式
 * @param time              时间 1-30 min// 被动模式
 * @param speedLevel        速度等级 1-12// 被动模式
 * @param spasmLevel        痉挛等级 1-12// 被动模式
 * @param resistance        阻力 1-12// 主动模式
 * @param intelligent       智能模式
 * @param forward           正转
 */
data class ShangXiaZhiParams(
    val passiveModule: Boolean,
    val time: Int,
    val speedLevel: Int,
    val spasmLevel: Int,
    val resistance: Int,
    val intelligent: Boolean,
    val forward: Boolean
) {
    /**
     * 转换成上下肢设备需要的参数
     */
    fun toTrunkCommandData(): TrunkCommandData {
        // 主被动模式
        val model = if (passiveModule) {
            0x01.toByte()
        } else {
            0x02.toByte()
        }

        // 智能
        val intelligence = if (intelligent) {
            0x00.toByte()
        } else {
            0x01.toByte()
        }

        // 方向
        val direction = if (forward) {
            0x00.toByte()
        } else {
            0x01.toByte()
        }
        return TrunkCommandData().also {
            it.model = model
            it.time = time.toByte()
            it.speed = speedLevel.toByte()
            it.spasm = spasmLevel.toByte()
            it.intelligence = intelligence
            it.resistance = resistance.toByte()
            it.direction = direction
        }
    }

    override fun toString(): String {
        return "${if (passiveModule) "被动模式" else "主动模式"}, ${time}分钟, ${if (intelligent) "智能模式" else ""}, ${if (forward) "正转" else "反转"},\n速度等级:$speedLevel, 痉挛等级:$spasmLevel, 阻力:$resistance"
    }
}