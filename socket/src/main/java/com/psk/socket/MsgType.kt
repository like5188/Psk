package com.psk.socket

import android.os.Parcel
import android.os.Parcelable

/*
PING（心跳）
EQUIPMENTS（设备信息）
 */
enum class MsgType() : Parcelable {
    PING, EQUIPMENTS;

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MsgType> {
        override fun createFromParcel(parcel: Parcel): MsgType {
            return MsgType.values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<MsgType?> {
            return arrayOfNulls(size)
        }
    }
}