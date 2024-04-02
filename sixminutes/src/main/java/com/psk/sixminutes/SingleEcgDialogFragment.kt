package com.psk.sixminutes

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.like.common.util.dp
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
        private const val KEY_PARAMS = "key_params"
        fun newInstance(sampleRate: Int, leadsName: String, params: String): SingleEcgDialogFragment {
            return SingleEcgDialogFragment().apply {
                arguments = bundleOf(
                    KEY_SAMPLE_RATE to sampleRate,
                    KEY_LEADS_NAME to leadsName,
                    KEY_PARAMS to params
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSingleEcgBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_single_ecg, container, true)
        val sampleRate = arguments?.getInt(KEY_SAMPLE_RATE) ?: 0
        val leadsName = arguments?.getString(KEY_LEADS_NAME) ?: ""
        val params = arguments?.getString(KEY_PARAMS) ?: ""
        mBinding.tvLeadsName.text = leadsName
        mBinding.tvEcgParams.text = params
        mBinding.ecgView.apply {
            setGridSize(15f.dp)
            setBgPainter(createBgPainter())
            setDataPainters(listOf(createDynamicDataPainter()))
            mBinding.ecgView.setSampleRate(sampleRate)
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
