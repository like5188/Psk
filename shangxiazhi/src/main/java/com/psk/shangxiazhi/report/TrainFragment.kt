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
        private const val KEY_SPEED_ARRAY = "key_speed_array"
        private const val KEY_TOTAL = "key_total"
        fun newInstance(speedArray: IntArray? = null, total: ShangXiaZhiAggregation? = null): TrainFragment {
            return TrainFragment().apply {
                arguments = bundleOf(
                    KEY_SPEED_ARRAY to speedArray, KEY_TOTAL to total
                )
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
        val speedArray = arguments?.getIntArray(KEY_SPEED_ARRAY)
        val total = arguments?.getSerializable(KEY_TOTAL) as? ShangXiaZhiAggregation
        mBinding.curveView.initChartData(speedArray?.toList(), 1)
        total?.apply {
            mBinding.tvMileage.text = decimalFormat.format(total.activeMil + total.passiveMil)
            mBinding.tvActiveMileage.text = decimalFormat.format(total.activeMil)
            mBinding.tvPassiveMileage.text = decimalFormat.format(total.passiveMil)
            mBinding.tvSpasmCount.text = "${total.spasm}次"
            mBinding.tvPower.text = "${decimalFormat.format(total.activeCal + total.passiveCal)}千卡"
            mBinding.tvSpeedAvg.text = total.speedArv.toString()
            mBinding.tvSpeedMin.text = total.speedMin.toString()
            mBinding.tvSpeedMax.text = total.speedMax.toString()
            mBinding.tvResAvg.text = total.speedArv.toString()
            mBinding.tvResMin.text = total.speedMin.toString()
            mBinding.tvResMax.text = total.speedMax.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
