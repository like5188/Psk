package com.psk.app.sixminutes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.base.addFragments
import com.psk.app.R
import com.psk.app.databinding.ActivitySixMinutesBinding
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.DevicesFragment

class SixMinutesActivity : AppCompatActivity() {
    private val mBinding: ActivitySixMinutesBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_six_minutes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        val devicesFragment = DevicesFragment.newInstance(
            1,
            mapOf(
                DeviceType.HeartRate to ("ER1 0455" to "E3:93:39:05:53:94"),
                DeviceType.BloodOxygen to ("O2 0382" to "C8:0C:CA:B3:E9:16"),
            )
        )
        addFragments(R.id.flContainer, 0, devicesFragment)
    }

}