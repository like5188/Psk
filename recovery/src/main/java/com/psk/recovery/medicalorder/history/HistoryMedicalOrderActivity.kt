package com.psk.recovery.medicalorder.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.recovery.R
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.databinding.ActivityHistoryMedicalOrderBinding
import com.seeker.luckychart.annotation.UIMode
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 医嘱回放界面
 */
class HistoryMedicalOrderActivity : AppCompatActivity() {
    companion object {
        fun start(medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice) {
            CommonApplication.sInstance.startActivity<HistoryMedicalOrderActivity>(
                "medicalOrderAndMonitorDevice" to medicalOrderAndMonitorDevice
            )
        }
    }

    @AutoWired
    lateinit var medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice

    private val mBinding: ActivityHistoryMedicalOrderBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_history_medical_order)
    }
    private val mViewModel: HistoryMedicalOrderViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        mViewModel.setMedicalOrderAndMonitorDevice(medicalOrderAndMonitorDevice)
        initView()
        collectUiState()
    }

    private fun initView() {
        mBinding.ecgChartView.setMode(UIMode.ERASE)
        mBinding.ecgChartView.initDefaultChartData(true, true)
        with(mBinding.textSeekBar) {
            onStart = {
                mViewModel.start()
            }
            onPause = {
                mViewModel.pause()
            }
            onMultipleChanged = {
                mViewModel.setMultiple(it)
            }
            onProgressChangedByTouch = {
                mViewModel.getHistoryDataByProgress(it)
            }
        }
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(HistoryMedicalOrderUiState::bloodOxygen) {
                mBinding.tvBloodOxygen.text = (it?.value ?: 0).toString()
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::bloodPressure) {
                mBinding.tvSBP.text = (it?.sbp ?: 0).toString()
                mBinding.tvDBP.text = (it?.dbp ?: 0).toString()
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::heartRate) {
                mBinding.tvHeartRate.text = (it ?: 0).toString()
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::ecgPointValue) {
                it ?: return@collectDistinctProperty
                mBinding.ecgChartView.updatePointsToRender(arrayOf(it))
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::ecgPointValues) {
                it ?: return@collectDistinctProperty
                // 手指触摸 SeekBar 改变进度时获取到的数据
                mBinding.ecgChartView.reset()
                mBinding.ecgChartView.updatePointsToRender(it.toTypedArray())
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::curTimeString) {
                mBinding.textSeekBar.setText(it)
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::progress) {
                mBinding.textSeekBar.setProgress(it)
            }
            collectDistinctProperty(HistoryMedicalOrderUiState::maxProgress) {
                mBinding.textSeekBar.setMax(it)
            }
            collectNotHandledEventProperty(HistoryMedicalOrderUiState::toastEvent) {
                this@HistoryMedicalOrderActivity.showToast(it)
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

}
