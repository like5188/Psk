package com.psk.sixminutes.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.psk.device.data.model.BloodPressure

@Entity
data class HealthInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long = 0,
    val bloodPressureBefore: BloodPressure? = null,// 运动前血压
    val bloodPressureAfter: BloodPressure? = null,// 运动后血压
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readParcelable(BloodPressure::class.java.classLoader),
        parcel.readParcelable(BloodPressure::class.java.classLoader),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(orderId)
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