package com.psk.shangxiazhi.scene

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.device.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivitySceneBinding
import com.psk.shangxiazhi.devices.SelectDeviceDialogFragment
import com.twsz.twsystempre.TrainScene
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 选择场景界面
 */
class SceneActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<SceneActivity>()
        }
    }

    private val mBinding: ActivitySceneBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_scene)
    }
    private val mViewModel: SceneViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.iv0.setOnClickListener {
            selectDevice(true, true, true, TrainScene.country)
        }
        mBinding.iv1.setOnClickListener {
            selectDevice(true, true, true, TrainScene.dust)
        }
        mBinding.iv2.setOnClickListener {
            selectDevice(true, true, true, TrainScene.lasa)
        }
        mBinding.iv3.setOnClickListener {
            selectDevice(true, true, true, TrainScene.sea)
        }
        mViewModel.initBle(this)
    }

    private fun selectDevice(
        existHeart: Boolean, existBloodOxygen: Boolean, existBloodPressure: Boolean, scene: TrainScene
    ) {
        SelectDeviceDialogFragment.newInstance(
            arrayOf(
                DeviceType.ShangXiaZhi,
                DeviceType.BloodOxygen,
                DeviceType.BloodPressure,
                DeviceType.HeartRate,
            )
        ).apply {
            onSelected = {
                mViewModel.start(
                    existHeart, existBloodOxygen, existBloodPressure, scene, resistanceInt = 1, passiveModule = true, timeInt = 2
                )
            }
            show(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
