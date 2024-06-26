package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.like.common.util.maximumFractionDigits
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.FragmentReportTrainBinding

class ShangXiaZhiFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_REPORT = "key_report"
        fun newInstance(shangXiaZhiReport: ShangXiaZhiReport?): ShangXiaZhiFragment {
            return ShangXiaZhiFragment().apply {
                arguments = bundleOf(KEY_REPORT to shangXiaZhiReport)
            }
        }
    }

    private lateinit var mBinding: FragmentReportTrainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_train, container, false)
        return mBinding.root
    }

    private fun formatDuration(duration: Int): String {
        val hour = duration / 3600
        val minute = duration % 3600 / 60
        val second = duration % 60
        return "${hour}小时${minute}分${second}秒"
    }

    override fun onLazyLoadData() {
        (arguments?.getSerializable(KEY_REPORT) as? ShangXiaZhiReport)?.apply {
            mBinding.tvDuration.text = formatDuration(activeDuration + passiveDuration)
            mBinding.tvActiveDuration.text = formatDuration(activeDuration)
            mBinding.tvPassiveDuration.text = formatDuration(passiveDuration)

            mBinding.tvMileage.text = (activeMil + passiveMil).maximumFractionDigits(2)
            mBinding.tvActiveMileage.text = activeMil.maximumFractionDigits(2)
            mBinding.tvPassiveMileage.text = passiveMil.maximumFractionDigits(2)

            mBinding.tvCal.text = activeCal.maximumFractionDigits(2)

            mBinding.tvSpasm.text = spasm.toString()
            mBinding.tvSpasmLevelAvg.text = spasmLevelArv.toString()
            mBinding.tvSpasmLevelMin.text = spasmLevelMin.toString()
            mBinding.tvSpasmLevelMax.text = spasmLevelMax.toString()

            mBinding.tvResistanceAvg.text = resistanceArv.toString()
            mBinding.tvResistanceMin.text = resistanceMin.toString()
            mBinding.tvResistanceMax.text = resistanceMax.toString()

            mBinding.tvPowerAvg.text = powerArv.toString()
            mBinding.tvPowerMin.text = powerMin.toString()
            mBinding.tvPowerMax.text = powerMax.toString()
            mBinding.curveViewPower.initChartData(powerList, "功率")

            mBinding.tvSpeedAvg.text = speedArv.toString()
            mBinding.tvSpeedMin.text = speedMin.toString()
            mBinding.tvSpeedMax.text = speedMax.toString()
            mBinding.curveViewSpeed.initChartData(speedList, "转速")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveViewPower.destroyDrawingCache()
        mBinding.curveViewPower.removeAllViews()
        mBinding.curveViewSpeed.destroyDrawingCache()
        mBinding.curveViewSpeed.removeAllViews()
    }

}
