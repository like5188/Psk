package com.psk.shangxiazhi.report

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityReportBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 报告界面
 */
class ReportActivity : AppCompatActivity() {
    companion object {
        fun start(speedList: IntArray) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "speedList" to speedList
            )
        }
    }

    @AutoWired
    val speedList: IntArray? = null
    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
    private val mViewModel: ReportViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        mBinding.curveView.initChartData(speedList?.toList(), 1)
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectEventProperty(ReportUiState::toastEvent) {
                showToast(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
