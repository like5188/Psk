package com.psk.device.data.model

import android.os.Parcel
import android.os.Parcelable
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong()
    ) {
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(time)
        parcel.writeInt(sbp)
        parcel.writeInt(dbp)
        parcel.writeLong(medicalOrderId)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "${sbp}/${dbp}"
    }

    companion object CREATOR : Parcelable.Creator<BloodPressure> {
        override fun createFromParcel(parcel: Parcel): BloodPressure {
            return BloodPressure(parcel)
        }

        override fun newArray(size: Int): Array<BloodPressure?> {
            return arrayOfNulls(size)
        }
    }


}