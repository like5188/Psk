package com.psk.recovery.medicalorder.execute

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.device.BleManager
import com.psk.recovery.R
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.databinding.ActivityExecuteMedicalOrderBinding
import com.seeker.luckychart.annotation.UIMode
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 执行医嘱界面
 */
class ExecuteMedicalOrderActivity : AppCompatActivity() {
    companion object {
        fun start(medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice) {
            CommonApplication.sInstance.startActivity<ExecuteMedicalOrderActivity>(
                "medicalOrderAndMonitorDevice" to medicalOrderAndMonitorDevice
            )
        }
    }

    @AutoWired
    lateinit var medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice

    private val mBinding: ActivityExecuteMedicalOrderBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_execute_medical_order)
    }
    private val bleManager by inject<BleManager>()
    private val mViewModel: ExecuteMedicalOrderViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        initView()
        collectUiState()
        lifecycleScope.launch {
            bleManager.onTip = {
                showToast(it.msg)
            }
            bleManager.init(this@ExecuteMedicalOrderActivity)
            mViewModel.setMedicalOrderAndMonitorDevice(medicalOrderAndMonitorDevice)
        }
    }

    private fun initView() {
        mBinding.ecgChartView.setMode(UIMode.ERASE)
        mBinding.ecgChartView.initDefaultChartData(true, true)
        mBinding.tvStartOrPause.setOnClickListener {
            mViewModel.startOrPause()
        }
        mBinding.tvFinish.setOnClickListener {
            mViewModel.finish()
            finish()
        }
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(ExecuteMedicalOrderUiState::bloodOxygen) {
                mBinding.tvBloodOxygen.text = (it?.value ?: 0).toString()
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::bloodPressure) {
                mBinding.tvSBP.text = (it?.sbp ?: 0).toString()
                mBinding.tvDBP.text = (it?.dbp ?: 0).toString()
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::heartRate) {
                mBinding.tvHeartRate.text = (it ?: 0).toString()
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::ecgPointValue) {
                it ?: return@collectDistinctProperty
                mBinding.ecgChartView.updatePointsToRender(arrayOf(it))
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::time) {
                mBinding.tvTime.text = it
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::startOrPause) {
                mBinding.tvStartOrPause.text = it
                if (it == "完成") {
                    bleManager.onDestroy()
                }
            }
            collectDistinctProperty(ExecuteMedicalOrderUiState::startOrPauseEnable) {
                mBinding.tvStartOrPause.isEnabled = it
            }
            collectNotHandledEventProperty(ExecuteMedicalOrderUiState::toastEvent) {
                this@ExecuteMedicalOrderActivity.showToast(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.ecgChartView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.ecgChartView.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.onDestroy()
    }

}
