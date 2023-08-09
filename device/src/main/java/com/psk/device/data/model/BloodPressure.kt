package com.psk.device.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BloodPressure(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    /**
     * 收缩压
     */
    val sbp: Int,
    /**
     * 舒张压
     */
    val dbp: Int,
    val medicalOrderId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BloodPressure

        if (sbp != other.sbp) return false
        if (dbp != other.dbp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sbp
        result = 31 * result + dbp
        return result
    }
}