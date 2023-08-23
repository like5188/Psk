package com.psk.shangxiazhi.scene

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
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
            selectDeviceAndStartGame(TrainScene.country)
        }
        mBinding.iv1.setOnClickListener {
            selectDeviceAndStartGame(TrainScene.dust)
        }
        mBinding.iv2.setOnClickListener {
            selectDeviceAndStartGame(TrainScene.lasa)
        }
        mBinding.iv3.setOnClickListener {
            selectDeviceAndStartGame(TrainScene.sea)
        }
        mViewModel.initBle(this)
    }

    private fun selectDeviceAndStartGame(scene: TrainScene) {
        SelectDeviceDialogFragment.newInstance(
            arrayOf(
                DeviceType.ShangXiaZhi,
                DeviceType.BloodOxygen,
                DeviceType.BloodPressure,
                DeviceType.HeartRate,
            )
        ).apply {
            onSelected = {
                if (it.isEmpty()) {
                    showToast("请先选择设备")
                } else {
                    val existHeart = it.containsKey(DeviceType.HeartRate)
                    val existBloodOxygen = it.containsKey(DeviceType.BloodOxygen)
                    val existBloodPressure = it.containsKey(DeviceType.BloodPressure)
                    mViewModel.start(
                        existHeart, existBloodOxygen, existBloodPressure, scene, resistanceInt = 1, passiveModule = true, timeInt = 2
                    )
                }
            }
            show(this@SceneActivity)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
