package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.BloodPressureReport
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.databinding.FragmentReportDevicesBinding

class OtherDevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_DATA = "key_data"
        fun newInstance(reports: List<IReport>?): OtherDevicesFragment {
            return OtherDevicesFragment().apply {
                arguments = bundleOf(
                    KEY_DATA to reports
                )
            }
        }
    }

    private lateinit var mBinding: FragmentReportDevicesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_devices, container, false)
        return mBinding.root
    }

    override fun onLazyLoadData() {
        val reports = arguments?.getSerializable(KEY_DATA) as? List<IReport>
        reports?.forEach {
            when (it) {
                is HeartRateReport -> {
                    mBinding.tvHeartRateAvg.text = it.arv.toString()
                    mBinding.tvHeartRateMin.text = it.min.toString()
                    mBinding.tvHeartRateMax.text = it.max.toString()
                    mBinding.curveView.initChartData(it.list, 2)
                }

                is BloodOxygenReport -> {
                    mBinding.tvBloodOxygen.text = it.value.toString()
                }

                is BloodPressureReport -> {
                    mBinding.tvDbp.text = it.dbp.toString()
                    mBinding.tvSbp.text = it.sbp.toString()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
