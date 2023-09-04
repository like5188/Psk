package com.psk.shangxiazhi.report

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.base.addFragments
import com.like.common.base.showFragment
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.TrainReport
import com.psk.shangxiazhi.databinding.ActivityReportBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 报告界面
 */
class ReportActivity : AppCompatActivity() {
    companion object {
        fun start(heartRateArray: IntArray? = null, trainReport: TrainReport? = null) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "heartRateArray" to heartRateArray,
                "trainReport" to trainReport
            )
        }
    }

    @AutoWired
    val heartRateArray: IntArray? = null

    @AutoWired
    val trainReport: TrainReport? = null

    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
    private val mViewModel: ReportViewModel by viewModel()
    private val trainFragment by lazy {
        TrainFragment.newInstance(trainReport)
    }
    private val devicesFragment by lazy {
        DevicesFragment.newInstance(heartRateArray)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        mBinding.tvTrain.setOnClickListener {
            mBinding.tvTrain.isSelected = true
            mBinding.tvDevices.isSelected = false
            showFragment(trainFragment)
        }
        mBinding.tvDevices.setOnClickListener {
            mBinding.tvTrain.isSelected = false
            mBinding.tvDevices.isSelected = true
            showFragment(devicesFragment)
        }
        addFragments(R.id.flContainer, 0, trainFragment, devicesFragment)
        mBinding.tvTrain.isSelected = true
        mBinding.tvDevices.isSelected = false
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectEventProperty(ReportUiState::toastEvent) {
                showToast(it)
            }
        }
    }

}
