package com.twsz.twsystempre

import android.os.Parcel
import android.os.Parcelable

/**
 * @param model         模式。0：主动；1：被动
 * @param speed         速度
 * @param speedLevel    速度档位
 * @param time          时间
 * @param mileage       里程
 * @param cal           卡路里
 * @param resistance    阻力
 * @param offset        偏移方向。-1：左；0：不偏移；1：右；
 * @param offsetValue   偏移值
 * @param spasm         痉挛次数
 * @param spasmLevel    痉挛强度
 * @param spasmFlag     痉挛提示
 * @param pause         暂停
 * @param over          结束
 * @param existHeart    是否有心电仪
 * @param connectBLE    心电仪是否连接
 * @param heart         心率
 * @param scene         场景。1："country"；2："dust"；3："lasa"；4："sea"；
 */
data class UnityValueModel(
    var model: Int = 0,
    var speed: Int = 0,
    var speedLevel: Int = 0,
    var time: String? = null,
    var mileage: String? = null,
    var cal: String? = null,
    var resistance: Int = 0,
    var offset: Int = 0,
    var offsetValue: Int = 0,
    var spasm: Int = 0,
    var spasmLevel: Int = 0,
    var spasmFlag: Int = 0,
    var pause: Int = 0,
    var over: Int = 0,
    var existHeart: Int = 0,
    var connectBLE: Int = 0,
    var heart: String? = null,
    var scene: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(model)
        parcel.writeInt(speed)
        parcel.writeInt(speedLevel)
        parcel.writeString(time)
        parcel.writeString(mileage)
        parcel.writeString(cal)
        parcel.writeInt(resistance)
        parcel.writeInt(offset)
        parcel.writeInt(offsetValue)
        parcel.writeInt(spasm)
        parcel.writeInt(spasmLevel)
        parcel.writeInt(spasmFlag)
        parcel.writeInt(pause)
        parcel.writeInt(over)
        parcel.writeInt(existHeart)
        parcel.writeInt(connectBLE)
        parcel.writeString(heart)
        parcel.writeString(scene)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UnityValueModel> {
        override fun createFromParcel(parcel: Parcel): UnityValueModel {
            return UnityValueModel(parcel)
        }

        override fun newArray(size: Int): Array<UnityValueModel?> {
            return arrayOfNulls(size)
        }
    }
}
