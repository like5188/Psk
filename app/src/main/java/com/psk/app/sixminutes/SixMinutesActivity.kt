package com.psk.app.sixminutes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.base.addFragments
import com.psk.app.R
import com.psk.app.databinding.ActivitySixMinutesBinding
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.DevicesFragment
import com.psk.sixminutes.model.BleInfo
import com.psk.sixminutes.model.SocketInfo

class SixMinutesActivity : AppCompatActivity() {
    private val mBinding: ActivitySixMinutesBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_six_minutes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        val devicesFragment = DevicesFragment.newInstance(
            1,
            listOf(
//                SocketInfo(DeviceType.HeartRate, "ICV200A", null, 7777),
//                BleInfo(DeviceType.HeartRate, "ER1 0455", "E3:93:39:05:53:94"),
                BleInfo(DeviceType.BloodOxygen, "O2 0214", "DF:04:89:AA:31:23"),
                BleInfo(DeviceType.BloodPressure, "BP0282A2210040311", "A4:C1:38:50:10:F1"),
            )
        )
        addFragments(R.id.flContainer, 0, devicesFragment)
    }

}