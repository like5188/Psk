package com.psk.recovery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeartRate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    /**
     * 心率
     */
    val values: IntArray,
    /**
     * 心电图的 y 坐标值
     */
    val coorYValues: FloatArray,
    val medicalOrderId: Long
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HeartRate) return false

        if (!values.contentEquals(other.values)) return false
        if (!coorYValues.contentEquals(other.coorYValues)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + coorYValues.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "HeartRate(id=$id, time=$time, valuesSize=${values.size}, coorYValuesSize=${coorYValues.size}, medicalOrderId=$medicalOrderId)"
    }
}