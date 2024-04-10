package com.psk.sixminutes

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.psk.sixminutes.databinding.DialogFragmentSingleEcgBinding
import com.psk.sixminutes.util.createBgPainter
import com.psk.sixminutes.util.createDynamicDataPainter

/**
 * 某个ECG导联放大的动态视图
 */
class SingleEcgDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_SAMPLE_RATE = "key_sample_rate"
        private const val KEY_LEADS_NAME = "key_leads_name"
        private const val KEY_MM_PER_S = "key_mm_per_s"
        private const val KEY_MM_PER_MV = "key_mm_per_mv"
        private const val KEY_GRID_SIZE = "key_grid_size"
        fun newInstance(sampleRate: Int, leadsName: String, mm_per_s: Int, mm_per_mv: Int, gridSize: Float): SingleEcgDialogFragment {
            return SingleEcgDialogFragment().apply {
                arguments = bundleOf(
                    KEY_SAMPLE_RATE to sampleRate,
                    KEY_LEADS_NAME to leadsName,
                    KEY_MM_PER_S to mm_per_s,
                    KEY_MM_PER_MV to mm_per_mv,
                    KEY_GRID_SIZE to gridSize,
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSingleEcgBinding
    private var mm_per_s = 0
    private var mm_per_mv = 0
    private var gridSize = 0f
    private var minGridSize = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_single_ecg, container, true)
        val sampleRate = arguments?.getInt(KEY_SAMPLE_RATE) ?: 0
        val leadsName = arguments?.getString(KEY_LEADS_NAME) ?: ""
        mm_per_s = arguments?.getInt(KEY_MM_PER_S) ?: 0
        mm_per_mv = arguments?.getInt(KEY_MM_PER_MV) ?: 0
        gridSize = arguments?.getFloat(KEY_GRID_SIZE) ?: 0f
        minGridSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, requireContext().resources.displayMetrics)
        mBinding.tvLeadsName.text = leadsName
        mBinding.tvMmPerS.text = "$mm_per_s mm/s"
        mBinding.tvMmPerMv.text = "$mm_per_mv mm/mV"
        mBinding.ecgView.apply {
            setGridSize(gridSize)
            setMmPerS(mm_per_s)
            setMmPerMv(mm_per_mv)
            setBgPainter(createBgPainter())
            setDataPainters(listOf(createDynamicDataPainter()))
            mBinding.ecgView.setSampleRate(sampleRate)
        }
        mBinding.btnMmPerS0.setOnClickListener {
            mm_per_s += 5
            if (mm_per_s > 50) {
                mm_per_s = 50
            }
            mBinding.ecgView.setMmPerS(mm_per_s)
            mBinding.tvMmPerS.text = "$mm_per_s mm/s"
        }
        mBinding.btnMmPerS1.setOnClickListener {
            mm_per_s -= 5
            if (mm_per_s < 5) {
                mm_per_s = 5
            }
            mBinding.ecgView.setMmPerS(mm_per_s)
            mBinding.tvMmPerS.text = "$mm_per_s mm/s"
        }
        mBinding.btnMmPerMv0.setOnClickListener {
            mm_per_mv += 5
            if (mm_per_mv > 20) {
                mm_per_mv = 20
            }
            mBinding.ecgView.setMmPerMv(mm_per_mv)
            mBinding.tvMmPerMv.text = "$mm_per_mv mm/mV"
        }
        mBinding.btnMmPerMv1.setOnClickListener {
            mm_per_mv -= 5
            if (mm_per_mv < 5) {
                mm_per_mv = 5
            }
            mBinding.ecgView.setMmPerMv(mm_per_mv)
            mBinding.tvMmPerMv.text = "$mm_per_mv mm/mV"
        }
        mBinding.btnGridSize0.setOnClickListener {
            gridSize += 1f
            if (gridSize > 20f) {
                gridSize = 20f
            }
            mBinding.ecgView.setGridSize(gridSize)
        }
        mBinding.btnGridSize1.setOnClickListener {
            gridSize -= 1f
            if (gridSize < minGridSize) {
                gridSize = minGridSize
            }
            mBinding.ecgView.setGridSize(gridSize)
        }
        return mBinding.root
    }

    override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
        // 宽高
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 位置
        layoutParams.gravity = Gravity.CENTER
        // 透明度
        layoutParams.dimAmount = 1f
    }

    fun addData(data: List<Float>) {
        if (::mBinding.isInitialized) {
            mBinding.ecgView.addData(listOf(data))
        }
    }

}
