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
            minTargetHeartRate: Int,
            maxTargetHeartRate: Int,
            met: Int,
            bloodPressureBefore: Int,
            bloodPressureAfter: Int
        ) {
            CommonApplication.sInstance.startActivity<ReportActivity>(
                "reports" to reports,
                "minTargetHeartRate" to minTargetHeartRate,
                "maxTargetHeartRate" to maxTargetHeartRate,
                "met" to met,
                "bloodPressureBefore" to bloodPressureBefore,
                "bloodPressureAfter" to bloodPressureAfter,
            )
        }
    }

    @AutoWired
    val reports: List<IReport>? = null

    @AutoWired
    val minTargetHeartRate: Int = 0

    @AutoWired
    val maxTargetHeartRate: Int = 0

    @AutoWired
    val met: Int = 0

    @AutoWired
    val bloodPressureBefore: Int = 0

    @AutoWired
    val bloodPressureAfter: Int = 0

    private val mBinding: ActivityReportBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_report)
    }
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
    }

}
