package com.psk.recovery.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MonitorDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 监视设备类型
     * 0：血氧仪；1：血压仪；2：心电；
     */
    val type: Int = -1,
    val medicalOrderId: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(type)
        parcel.writeLong(medicalOrderId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonitorDevice> {
        override fun createFromParcel(parcel: Parcel): MonitorDevice {
            return MonitorDevice(parcel)
        }

        override fun newArray(size: Int): Array<MonitorDevice?> {
            return arrayOfNulls(size)
        }
    }
}