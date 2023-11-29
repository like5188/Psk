package com.psk.device.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HealthInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long = 0,
    val age: Int = 0,// 年龄
    val weight: Int = 0,// 体重（kg）
    val met: Float = 0f,// met值
    val minTargetHeartRate: Int = 0,// 最小靶心率
    val maxTargetHeartRate: Int = 0,// 最大靶心率
    val bloodPressureBefore: BloodPressure? = null,// 运动前血压
    val bloodPressureAfter: BloodPressure? = null,// 运动后血压
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(BloodPressure::class.java.classLoader),
        parcel.readParcelable(BloodPressure::class.java.classLoader),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(orderId)
        parcel.writeInt(age)
        parcel.writeInt(weight)
        parcel.writeFloat(met)
        parcel.writeInt(minTargetHeartRate)
        parcel.writeInt(maxTargetHeartRate)
        parcel.writeParcelable(bloodPressureBefore, flags)
        parcel.writeParcelable(bloodPressureAfter, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HealthInfo> {
        override fun createFromParcel(parcel: Parcel): HealthInfo {
            return HealthInfo(parcel)
        }

        override fun newArray(size: Int): Array<HealthInfo?> {
            return arrayOfNulls(size)
        }
    }


}