package com.psk.shangxiazhi.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseLazyFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.FragmentReportTrainBinding
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class ShangXiaZhiFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_DATA = "key_data"
        fun newInstance(shangXiaZhiReport: ShangXiaZhiReport?): ShangXiaZhiFragment {
            return ShangXiaZhiFragment().apply {
                arguments = bundleOf(KEY_DATA to shangXiaZhiReport)
            }
        }
    }

    private lateinit var mBinding: FragmentReportTrainBinding
    private val decimalFormat by inject<DecimalFormat>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_train, container, false)
        return mBinding.root
    }

    private fun formatDuration(duration: Int): String {
        val min = duration / 60
        val sec = duration % 60
        val minStr = if (min < 10) "0$min" else "$min"
        val secStr = if (sec < 10) "0$sec" else "$sec"
        return if (min >= 1) "${minStr}分${secStr}秒" else "${secStr}秒"
    }

    override fun onLazyLoadData() {
        (arguments?.getSerializable(KEY_DATA) as? ShangXiaZhiReport)?.apply {
            mBinding.tvDuration.text = formatDuration(activeDuration + passiveDuration)
            mBinding.tvActiveDuration.text = formatDuration(activeDuration)
            mBinding.tvPassiveDuration.text = formatDuration(passiveDuration)

            mBinding.tvMileage.text = decimalFormat.format(activeMil + passiveMil)
            mBinding.tvActiveMileage.text = decimalFormat.format(activeMil)
            mBinding.tvPassiveMileage.text = decimalFormat.format(passiveMil)

            mBinding.tvPower.text = decimalFormat.format(activeCal + passiveCal)
            mBinding.tvActivePower.text = decimalFormat.format(activeCal)
            mBinding.tvPassivePower.text = decimalFormat.format(passiveCal)

            mBinding.tvSpasmCount.text = spasm.toString()
            mBinding.tvSpasmLevelAvg.text = spasmLevelArv.toString()
            mBinding.tvSpasmLevelMin.text = spasmLevelMin.toString()
            mBinding.tvSpasmLevelMax.text = spasmLevelMax.toString()

            mBinding.tvResAvg.text = speedArv.toString()
            mBinding.tvResMin.text = speedMin.toString()
            mBinding.tvResMax.text = speedMax.toString()

            mBinding.tvSpeedAvg.text = speedArv.toString()
            mBinding.tvSpeedMin.text = speedMin.toString()
            mBinding.tvSpeedMax.text = speedMax.toString()
            mBinding.curveView.initChartData(speedList, 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.curveView.destroyDrawingCache()
        mBinding.curveView.removeAllViews()
    }

}
