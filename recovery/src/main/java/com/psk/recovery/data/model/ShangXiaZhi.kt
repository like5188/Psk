package com.psk.recovery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ShangXiaZhi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    var model: Byte = 0,//1：被动 2：主动  这个是从蓝牙设备获取到的数据
    val speedLevel: Int = 0,
    val speedValue: Int = 0,
    val offset: Int = 0,
    val spasmNum: Int = 0,
    val spasmLevel: Int = 0,
    val res: Int = 0,
    val intelligence: Byte = 0,
    val direction: Byte = 0,
    val medicalOrderId: Long = 0
) {
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
        return result
    }
}