package com.psk.shangxiazhi.report

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.base.addFragments
import com.like.common.base.showFragment
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.showToast
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.ActivityReportBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 报告界面
 */
class ReportActivity : AppCompatActivity() {
    companion object {
        fun start(medicalOrderId: Long?) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "medicalOrderId" to medicalOrderId,
            )
        }
    }

    @AutoWired
    val medicalOrderId: Long? = null

    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
    private val mViewModel: ReportViewModel by viewModel()
    private lateinit var shangXiaZhiFragment: ShangXiaZhiFragment
    private lateinit var otherDevicesFragment: OtherDevicesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        mBinding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            when (checkedId) {
                R.id.btnTrain -> {
                    showFragment(shangXiaZhiFragment)
                }

                R.id.btnDevices -> {
                    showFragment(otherDevicesFragment)
                }
            }
        }
        if (medicalOrderId == null) {
            showToast("获取报告失败")
            finish()
            return
        }
        lifecycleScope.launch {
            val reports = mViewModel.getReports(medicalOrderId)
            val healthInfo = mViewModel.getHealthInfo(medicalOrderId)
            shangXiaZhiFragment = ShangXiaZhiFragment.newInstance(reports.firstOrNull {
                it is ShangXiaZhiReport
            } as? ShangXiaZhiReport)
            otherDevicesFragment = OtherDevicesFragment.newInstance(healthInfo, reports.filter {
                it !is ShangXiaZhiReport
            })
            addFragments(R.id.flContainer, -1, shangXiaZhiFragment, otherDevicesFragment)
            mBinding.toggleGroup.check(R.id.btnTrain)
        }
    }

}
