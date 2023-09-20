package com.psk.shangxiazhi.report

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.base.addFragments
import com.like.common.base.showFragment
import com.like.common.util.AutoWired
import com.like.common.util.injectForIntentExtras
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.ActivityReportBinding

/**
 * 报告界面
 */
class ReportActivity : AppCompatActivity() {
    companion object {
        fun start(
            reports: List<IReport>?,
            healthInfo: HealthInfo?
        ) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "reports" to reports,
                "healthInfo" to healthInfo,
            )
        }
    }

    @AutoWired
    val reports: List<IReport>? = null

    @AutoWired
    val healthInfo: HealthInfo? = null

    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
    private val shangXiaZhiFragment by lazy {
        ShangXiaZhiFragment.newInstance(reports?.firstOrNull {
            it is ShangXiaZhiReport
        } as? ShangXiaZhiReport)
    }
    private val otherDevicesFragment by lazy {
        OtherDevicesFragment.newInstance(healthInfo, reports?.filter {
            it !is ShangXiaZhiReport
        })
    }

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
        addFragments(R.id.flContainer, -1, shangXiaZhiFragment, otherDevicesFragment)
        mBinding.toggleGroup.check(R.id.btnTrain)
    }

}
