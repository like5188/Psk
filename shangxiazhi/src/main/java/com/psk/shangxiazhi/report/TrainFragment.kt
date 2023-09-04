package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.ShangXiaZhiAggregation
import com.psk.shangxiazhi.databinding.FragmentReportTrainBinding
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class TrainFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_AGGREGATION = "key_aggregation"
        fun newInstance(aggregation: ShangXiaZhiAggregation? = null): TrainFragment {
            return TrainFragment().apply {
                arguments = bundleOf(KEY_AGGREGATION to aggregation)
            }
        }
    }

    private lateinit var mBinding: FragmentReportTrainBinding
    private val decimalFormat by inject<DecimalFormat>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_train, container, false)
        return mBinding.root
    }

    override fun onLazyLoadData() {
        (arguments?.getSerializable(KEY_AGGREGATION) as? ShangXiaZhiAggregation)?.apply {
            mBinding.tvMileage.text = decimalFormat.format(activeMil + passiveMil)
            mBinding.tvActiveMileage.text = decimalFormat.format(activeMil)
            mBinding.tvPassiveMileage.text = decimalFormat.format(passiveMil)
            mBinding.tvSpasmCount.text = "${spasm}次"
            mBinding.tvPower.text = "${decimalFormat.format(activeCal + passiveCal)}千卡"
            mBinding.tvSpeedAvg.text = speedArv.toString()
            mBinding.tvSpeedMin.text = speedMin.toString()
            mBinding.tvSpeedMax.text = speedMax.toString()
            mBinding.tvResAvg.text = speedArv.toString()
            mBinding.tvResMin.text = speedMin.toString()
            mBinding.tvResMax.text = speedMax.toString()
            mBinding.curveView.initChartData(speedList, 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
