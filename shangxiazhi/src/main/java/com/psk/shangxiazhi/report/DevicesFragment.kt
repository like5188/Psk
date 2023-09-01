package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.FragmentReportDevicesBinding

class DevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_DATA = "KEY_DATA"
        fun newInstance(heartRateArray: IntArray?): DevicesFragment {
            return DevicesFragment().apply {
                arguments = bundleOf(
                    KEY_DATA to heartRateArray
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
        val heartRateArray = arguments?.getIntArray(KEY_DATA)
        mBinding.curveView.initChartData(heartRateArray?.toList(), 2)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
