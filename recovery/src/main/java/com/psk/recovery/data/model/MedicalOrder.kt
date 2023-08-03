package com.psk.recovery.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MedicalOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long = -1,// 患者id
    val planExecuteTime: Long = 0,// 计划执行时间
    val planInterval: Long = 0,// 计划执行时长
    val startTime: Long = 0,// 实际开始时间
    val endTime: Long = 0,// 实际结束时间
    val remainInterval: Long = planInterval,// 剩余时长
    val status: Int = 0,// 医嘱状态。0：未开始；1：进行中；2：已完成；
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt()
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MedicalOrder) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(patientId)
        parcel.writeLong(planExecuteTime)
        parcel.writeLong(planInterval)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeLong(remainInterval)
        parcel.writeInt(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MedicalOrder> {
        override fun createFromParcel(parcel: Parcel): MedicalOrder {
            return MedicalOrder(parcel)
        }

        override fun newArray(size: Int): Array<MedicalOrder?> {
            return arrayOfNulls(size)
        }
    }

}