package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.FragmentReportTrainBinding

class TrainFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_DATA = "KEY_DATA"
        fun newInstance(speedArray: IntArray?): TrainFragment {
            return TrainFragment().apply {
                arguments = bundleOf(
                    KEY_DATA to speedArray
                )
            }
        }
    }

    private lateinit var mBinding: FragmentReportTrainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_train, container, false)
        return mBinding.root
    }

    override fun onLazyLoadData() {
        val speedArray = arguments?.getIntArray(KEY_DATA)
        mBinding.curveView.initChartData(speedArray?.toList(), 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
