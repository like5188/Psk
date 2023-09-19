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
        mBinding.deviceCardView.setOnClickListener {
            mViewModel.selectDevices(this)
        }
        mBinding.sceneCardView.setOnClickListener {
            mViewModel.selectTrainScene(this)
        }
        mBinding.personInfoCardView.setOnClickListener {
            mViewModel.setPersonInfo(this)
        }
        mBinding.targetHeartRateCardView.setOnClickListener {
            mViewModel.measureTargetHeart(this)
        }
        mBinding.bloodPressureBeforeCardView.setOnClickListener {
            mViewModel.measureBloodPressureBefore(this)
        }
        mBinding.bloodPressureAfterCardView.setOnClickListener {
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
                mBinding.sceneCardView.gone()
                mBinding.personInfoCardView.gone()
                mBinding.bloodPressureBeforeCardView.gone()
                mBinding.bloodPressureAfterCardView.gone()
                mBinding.targetHeartRateCardView.gone()
                if (it.isNullOrEmpty()) {
                    return@collectDistinctProperty
                }
                mBinding.sceneCardView.visible()
                mBinding.personInfoCardView.visible()
                if (it.containsKey(DeviceType.BloodPressure)) {
                    mBinding.bloodPressureBeforeCardView.visible()
                    mBinding.bloodPressureAfterCardView.visible()
                }
                if (it.containsKey(DeviceType.HeartRate)) {
                    mBinding.targetHeartRateCardView.visible()
                }
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }

}
