package com.psk.shangxiazhi.train

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.gone
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.like.common.util.visible
import com.psk.ble.DeviceType
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityTrainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 训练界面
 */
class TrainActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<TrainActivity>()
        }
    }

    private val mBinding: ActivityTrainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_train)
    }
    private val mViewModel: TrainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.bindGameManagerService(this)
        mBinding.llDevice.setOnClickListener {
            mViewModel.selectDevices(this)
        }
        mBinding.llScene.setOnClickListener {
            mViewModel.selectTrainScene(this)
        }
        mBinding.llPersonInfo.setOnClickListener {
            mViewModel.setPersonInfo(this)
        }
        mBinding.llTargetHeartRate.setOnClickListener {
            mViewModel.measureTargetHeart(this)
        }
        mBinding.llBloodPressureBefore.setOnClickListener {
            mViewModel.measureBloodPressureBefore(this)
        }
        mBinding.llBloodPressureAfter.setOnClickListener {
            mViewModel.measureBloodPressureAfter(this)
        }
        mBinding.btnTrain.setOnClickListener {
            mViewModel.train()
        }
        mBinding.btnReport.setOnClickListener {
            mViewModel.report()
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectNotHandledEventProperty(TrainUiState::toastEvent) {
                showToast(it)
            }
            collectDistinctProperty(TrainUiState::gameManagerService) {
                it ?: return@collectDistinctProperty
                it.initBle(this@TrainActivity)
            }
            collectDistinctProperty(TrainUiState::deviceMap) {
                mBinding.llScene.gone()
                mBinding.llPersonInfo.gone()
                mBinding.llBloodPressureBefore.gone()
                mBinding.llBloodPressureAfter.gone()
                mBinding.llTargetHeartRate.gone()
                mBinding.btnTrain.gone()
                mBinding.btnReport.gone()
                if (it.isNullOrEmpty()) {
                    return@collectDistinctProperty
                }
                mBinding.llScene.visible()
                mBinding.llPersonInfo.visible()
                if (it.containsKey(DeviceType.BloodPressure)) {
                    mBinding.llBloodPressureBefore.visible()
                    mBinding.llBloodPressureAfter.visible()
                }
                if (it.containsKey(DeviceType.HeartRate)) {
                    mBinding.llTargetHeartRate.visible()
                }
                mBinding.btnTrain.visible()
                val sb = StringBuilder()
                it.forEach {
                    val deviceType = it.key
                    val deviceName = it.value.name
                    if (sb.isNotEmpty()) {
                        sb.append("\n")
                    }
                    sb.append(deviceType.des).append(":").append(deviceName)
                }
                mBinding.tvDevice.text = sb.toString()
            }
            collectDistinctProperty(TrainUiState::healthInfo) {
                it ?: return@collectDistinctProperty
                val sb = StringBuilder()
                if (it.age > 0) {
                    sb.append("年龄:").append(it.age)
                }
                if (it.weight > 0) {
                    if (sb.isNotEmpty()) {
                        sb.append(", ")
                    }
                    sb.append("体重:").append(it.weight)
                }
                mBinding.tvPersonInfo.text = sb.toString()
                mBinding.tvTargetHeartRate.text = if (it.minTargetHeartRate == 0 || it.maxTargetHeartRate == 0) {
                    ""
                } else {
                    "${it.minTargetHeartRate}~${it.maxTargetHeartRate}"
                }
                mBinding.tvBloodPressureBefore.text = it.bloodPressureBefore?.toString() ?: ""
                mBinding.tvBloodPressureAfter.text = it.bloodPressureAfter?.toString() ?: ""
            }
            collectDistinctProperty(TrainUiState::scene) {
                mBinding.tvScene.text = it?.des ?: ""
            }
            collectDistinctProperty(TrainUiState::reports) {
                if (!it.isNullOrEmpty()) {
                    mBinding.btnTrain.gone()
                    mBinding.btnReport.visible()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }

}
