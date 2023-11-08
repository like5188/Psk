package com.psk.socket

import android.os.Parcel
import android.os.Parcelable

/*
{
  "type":"equipments",
  "data":[
    {
      "client_id":"7f000001096800000037",
      "equipment_id":661114904,
      "start_micro_time":"1699321106000",
      "serial_no":8084
    }
  ]
}
 */
data class Msg(
    val type: MsgType?,
    val data: List<Data>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(MsgType::class.java.classLoader),
        parcel.createTypedArrayList(Data)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(type, flags)
        parcel.writeTypedList(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Msg> {
        override fun createFromParcel(parcel: Parcel): Msg {
            return Msg(parcel)
        }

        override fun newArray(size: Int): Array<Msg?> {
            return arrayOfNulls(size)
        }
    }

}

data class Data(
    val client_id: String?,
    val equipment_id: Int?,
    val start_micro_time: String?,
    val serial_no: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(client_id)
        parcel.writeValue(equipment_id)
        parcel.writeString(start_micro_time)
        parcel.writeValue(serial_no)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Data> {
        override fun createFromParcel(parcel: Parcel): Data {
            return Data(parcel)
        }

        override fun newArray(size: Int): Array<Data?> {
            return arrayOfNulls(size)
        }
    }

}