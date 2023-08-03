package com.psk.recovery.data.model.embedded

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.like.recyclerview.model.IRecyclerViewItem
import com.psk.recovery.BR
import com.psk.recovery.R
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice

/**
 * 医嘱数据组合
 */
data class MedicalOrderAndMonitorDevice(
    @Embedded val medicalOrder: MedicalOrder,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicalOrderId"
    )
    val monitorDevices: List<MonitorDevice>,
) : IRecyclerViewItem, Parcelable {
    override val layoutId: Int get() = R.layout.item_medical_order
    override val variableId: Int get() = BR.item

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(MedicalOrder::class.java.classLoader)!!,
        parcel.createTypedArrayList(MonitorDevice)!!
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MedicalOrderAndMonitorDevice) return false

        if (medicalOrder != other.medicalOrder) return false

        return true
    }

    override fun hashCode(): Int {
        return medicalOrder.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(medicalOrder, flags)
        parcel.writeTypedList(monitorDevices)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MedicalOrderAndMonitorDevice> {
        override fun createFromParcel(parcel: Parcel): MedicalOrderAndMonitorDevice {
            return MedicalOrderAndMonitorDevice(parcel)
        }

        override fun newArray(size: Int): Array<MedicalOrderAndMonitorDevice?> {
            return arrayOfNulls(size)
        }
    }

}