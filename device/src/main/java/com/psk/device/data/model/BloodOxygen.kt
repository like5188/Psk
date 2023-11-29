package com.psk.device.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BloodOxygen(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createTime: Long = System.currentTimeMillis(),
    val value: Int,
    val orderId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BloodOxygen

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }
}