package com.psk.shangxiazhi.train

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.like.common.util.gone
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.like.common.util.startActivity
import com.like.common.util.visible
import com.psk.common.CommonApplication
import com.psk.device.data.model.DeviceType
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
    private var bloodPressureMeasureType: Int = 0// 运动中血压测量方式。0：手动测量；1：自动测量（间隔5分钟测量）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.bindGameManagerService(this)
        mBinding.deviceCardView.setOnClickListener {
            mViewModel.selectDevices(this)
        }
        mBinding.sceneCardView.setOnClickListener {
            mViewModel.selectTrainScene(this)
        }
        mBinding.targetHeartRateCardView.setOnClickListener {
            mViewModel.measureTargetHeart(this)
        }
        mBinding.bloodPressureBeforeCardView.setOnClickListener {
            mViewModel.measureBloodPressureBefore(this)
        }
        mBinding.btnTrain.setOnClickListener {
            mViewModel.train(bloodPressureMeasureType)
        }
        mBinding.bloodPressureMeasureTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.manualRb -> bloodPressureMeasureType = 0
                R.id.autoRb -> bloodPressureMeasureType = 1
            }
        }
        mBinding.weightCardView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mBinding.etWeight.requestFocus()
            }
        }
        mBinding.etWeight.doAfterTextChanged {
            mViewModel.setWeight(it?.toString()?.toIntOrNull() ?: 0)
        }
        mBinding.ageCardView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                mBinding.etAge.requestFocus()
            }
        }
        mBinding.etAge.doAfterTextChanged {
            mViewModel.setAge(it?.toString()?.toIntOrNull() ?: 0)
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectNotHandledEventProperty(TrainUiState::toastEvent) {
                showToast(toastEvent = it)
            }
            collectDistinctProperty(TrainUiState::deviceMap) {
                mBinding.sceneCardView.gone()
                mBinding.weightCardView.gone()
                mBinding.ageCardView.gone()
                mBinding.bloodPressureBeforeCardView.gone()
                mBinding.targetHeartRateCardView.gone()
                mBinding.bloodPressureMeasureTypeCardView.gone()
                if (it.isNullOrEmpty()) {
                    return@collectDistinctProperty
                }
                mBinding.sceneCardView.visible()
                mBinding.weightCardView.visible()
                if (it.containsKey(DeviceType.BloodPressure)) {
                    mBinding.bloodPressureBeforeCardView.visible()
                    mBinding.bloodPressureMeasureTypeCardView.visible()
                }
                if (it.containsKey(DeviceType.HeartRate)) {
                    mBinding.ageCardView.visible()
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
            collectDistinctProperty(TrainUiState::scene) {
                mBinding.tvScene.text = it?.des ?: ""
                if (it != null) {
                    // 这里因为跳转到 Activity 返回后，sceneCardView 会失去焦点。
                    mBinding.sceneCardView.requestFocus()
                }
            }
            collectDistinctProperty(TrainUiState::healthInfo) {
                it ?: return@collectDistinctProperty
                mBinding.tvTargetHeartRate.text = if (it.minTargetHeartRate == 0 || it.maxTargetHeartRate == 0) {
                    ""
                } else {
                    "${it.minTargetHeartRate}~${it.maxTargetHeartRate}"
                }
                mBinding.tvBloodPressureBefore.text = it.bloodPressureBefore?.toString() ?: ""
                mBinding.etAge.setText(if (it.age == 0) "" else it.age.toString())
                mBinding.etWeight.setText(if (it.weight == 0) "" else it.weight.toString())
            }
            collectDistinctProperty(TrainUiState::isTrainCompleted) {
                if (!it) {
                    return@collectDistinctProperty
                }
                // 训练完成
                // 如果没有血压仪
                if (mViewModel.uiState.value.deviceMap?.containsKey(DeviceType.BloodPressure) != true) {
                    mViewModel.report()
                    finish()
                    return@collectDistinctProperty
                }
                // 如果有血压仪
                val dialog =
                    AlertDialog.Builder(this@TrainActivity).setMessage("是否进行运动后血压测试?").setNegativeButton("取消") { _, _ ->
                        mViewModel.report()
                        finish()
                    }.setPositiveButton("去测量") { _, _ ->
                        mViewModel.measureBloodPressureAfter(this@TrainActivity) {
                            mViewModel.report()
                            finish()
                        }
                    }.create()
                dialog.setOnKeyListener { dialog, keyCode, event ->
                    // 返回键点击事件处理
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        mViewModel.report()
                        finish()
                    }
                    false
                }
                dialog.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }

}
