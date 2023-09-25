package com.psk.shangxiazhi.train

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.gone
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.like.common.util.visible
import com.psk.ble.BleManager
import com.psk.ble.DeviceType
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityTrainBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
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
    private val bleManager by inject<BleManager>()
    private var bloodPressureMeasureType: Int = 0// 运动中血压测量方式。0：手动测量；1：自动测量（间隔5分钟测量）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            bleManager.onTip = { Log.e("bleManager", "onTip ${it.msg}") }
            bleManager.requestEnvironment(this@TrainActivity)
        }
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
            mViewModel.train(bloodPressureMeasureType)
        }
        mBinding.btnReport.setOnClickListener {
            mViewModel.report()
        }
        mBinding.bloodPressureMeasureTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.manualRb -> bloodPressureMeasureType = 0
                R.id.autoRb -> bloodPressureMeasureType = 1
            }
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectNotHandledEventProperty(TrainUiState::toastEvent) {
                showToast(it)
            }
            collectDistinctProperty(TrainUiState::deviceMap) {
                mBinding.sceneCardView.gone()
                mBinding.personInfoCardView.gone()
                mBinding.bloodPressureBeforeCardView.gone()
                mBinding.bloodPressureAfterCardView.gone()
                mBinding.targetHeartRateCardView.gone()
                mBinding.bloodPressureMeasureTypeCardView.gone()
                if (it.isNullOrEmpty()) {
                    return@collectDistinctProperty
                }
                mBinding.sceneCardView.visible()
                mBinding.personInfoCardView.visible()
                if (it.containsKey(DeviceType.BloodPressure)) {
                    mBinding.bloodPressureBeforeCardView.visible()
                    mBinding.bloodPressureAfterCardView.visible()
                    mBinding.bloodPressureMeasureTypeCardView.visible()
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
                if (it != null) {
                    // 这里因为跳转到 Activity 返回后，sceneCardView 会失去焦点。
                    mBinding.sceneCardView.requestFocus()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }

}
