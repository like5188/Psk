package com.psk.shangxiazhi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 训练
 */
@Entity
data class OrderInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createTime: Long = System.currentTimeMillis(),
    val orderId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderInfo) return false

        if (orderId != other.orderId) return false

        return true
    }

    override fun hashCode(): Int {
        return orderId.hashCode()
    }
}