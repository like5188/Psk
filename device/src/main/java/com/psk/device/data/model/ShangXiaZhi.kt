package com.psk.device.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// 上下肢传递过来的数据
@Entity
data class ShangXiaZhi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    var model: Byte = 0,//模式： 0x01 表示  被动 0x02 表示  主动
    val speedLevel: Int = 0,//速度档位：范围0~12           十六进制：0x00~0x3c
    val speedValue: Int = 0,//速度圈数：范围0~
    val offset: Int = 0,//偏移：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
    val spasmNum: Int = 0,//痉挛次数：范围0~
    val spasmLevel: Int = 0,//痉挛等级：范围1~12   十六进制：0x01~0x0c
    val res: Int = 0,//阻力：范围1~12           十六进制：0x01~0x0c
    val intelligence: Byte = 0,//智能： 0x40 表示 关闭 0x41 表示 打开
    val direction: Byte = 0,//正反转： 0x50 表示 反转 0x51 表示 正转
    val medicalOrderId: Long = 0,
) {
    @Ignore
    var curTime: String = ""//当前计时。用于游戏界面显示。其实上下肢没有返回这个数据，是自己计算的。
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShangXiaZhi) return false

        if (model != other.model) return false
        if (speedLevel != other.speedLevel) return false
        if (speedValue != other.speedValue) return false
        if (offset != other.offset) return false
        if (spasmNum != other.spasmNum) return false
        if (spasmLevel != other.spasmLevel) return false
        if (res != other.res) return false
        if (intelligence != other.intelligence) return false
        if (direction != other.direction) return false
        if (curTime != other.curTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.toInt()
        result = 31 * result + speedLevel
        result = 31 * result + speedValue
        result = 31 * result + offset
        result = 31 * result + spasmNum
        result = 31 * result + spasmLevel
        result = 31 * result + res
        result = 31 * result + intelligence
        result = 31 * result + direction
        result = 31 * result + curTime.hashCode()
        return result
    }
}