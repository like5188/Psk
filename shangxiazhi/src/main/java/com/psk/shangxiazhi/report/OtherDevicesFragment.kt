package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.BloodPressureReport
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.databinding.FragmentReportDevicesBinding
import java.text.DecimalFormat

class OtherDevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_HEALTH_INFO = "key_health_info"
        private const val KEY_REPORTS = "key_reports"
        fun newInstance(healthInfo: HealthInfo?, reports: List<IReport>?): OtherDevicesFragment {
            return OtherDevicesFragment().apply {
                arguments = bundleOf(
                    KEY_HEALTH_INFO to healthInfo,
                    KEY_REPORTS to reports
                )
            }
        }
    }

    private lateinit var mBinding: FragmentReportDevicesBinding
    private val decimalFormat = DecimalFormat("######0.0")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_devices, container, false)
        return mBinding.root
    }

    override fun onLazyLoadData() {
        arguments?.getParcelable<HealthInfo>(KEY_HEALTH_INFO)?.apply {
            mBinding.tvTargetHeartRate.text = "$minTargetHeartRate~$maxTargetHeartRate"
            mBinding.tvMet.text = decimalFormat.format(met)
            mBinding.tvBloodPressureBefore.text = if (bloodPressureBefore == null) {
                ""
            } else {
                "${bloodPressureBefore!!.sbp}/${bloodPressureBefore!!.dbp}"
            }
            mBinding.tvBloodPressureAfter.text = if (bloodPressureAfter == null) {
                ""
            } else {
                "${bloodPressureAfter!!.sbp}/${bloodPressureAfter!!.dbp}"
            }
        }
        (arguments?.getSerializable(KEY_REPORTS) as? List<IReport>)?.forEach {
            when (it) {
                is HeartRateReport -> {
                    mBinding.tvHeartRateAvg.text = it.arv.toString()
                    mBinding.tvHeartRateMin.text = it.min.toString()
                    mBinding.tvHeartRateMax.text = it.max.toString()
                    mBinding.curveViewHeartRate.initChartData(it.list, "心率")
                }

                is BloodOxygenReport -> {
                    mBinding.tvBloodOxygen.text = it.value.toString()
                }

                is BloodPressureReport -> {
                    mBinding.tvBloodPressure.text = "${it.sbp}/${it.dbp}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveViewHeartRate.destroyDrawingCache()
        mBinding.curveViewHeartRate.removeAllViews()
    }

}
