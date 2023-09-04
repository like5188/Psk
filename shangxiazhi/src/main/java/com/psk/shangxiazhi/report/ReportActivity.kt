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
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.ActivityReportBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 报告界面
 */
class ReportActivity : AppCompatActivity() {
    companion object {
        fun start(reports: List<IReport>) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "reports" to reports
            )
        }
    }

    @AutoWired
    val reports: List<IReport>? = null

    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
    private val mViewModel: ReportViewModel by viewModel()
    private val shangXiaZhiFragment by lazy {
        ShangXiaZhiFragment.newInstance(reports?.firstOrNull {
            it is ShangXiaZhiReport
        } as? ShangXiaZhiReport)
    }
    private val otherDevicesFragment by lazy {
        OtherDevicesFragment.newInstance(reports?.filter {
            it !is ShangXiaZhiReport
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        mBinding.tvTrain.setOnClickListener {
            mBinding.tvTrain.isSelected = true
            mBinding.tvDevices.isSelected = false
            showFragment(shangXiaZhiFragment)
        }
        mBinding.tvDevices.setOnClickListener {
            mBinding.tvTrain.isSelected = false
            mBinding.tvDevices.isSelected = true
            showFragment(otherDevicesFragment)
        }
        addFragments(R.id.flContainer, 0, shangXiaZhiFragment, otherDevicesFragment)
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