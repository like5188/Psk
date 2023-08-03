package com.psk.recovery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BloodOxygen(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long = System.currentTimeMillis() / 1000,
    val value: Int,
    val medicalOrderId: Long
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