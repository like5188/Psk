package com.psk.shangxiazhi.device

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.like.common.util.toIntOrDefault
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentMeasureTargetHeartRateBinding

/**
 * 测量靶心率
 */
class MeasureTargetHeartRateDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        fun newInstance(): MeasureTargetHeartRateDialogFragment {
            return MeasureTargetHeartRateDialogFragment()
        }
    }

    private lateinit var mBinding: DialogFragmentMeasureTargetHeartRateBinding
    var onSelected: ((Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_measure_target_heart_rate, container, true)
        mBinding.btnMeasure.setOnClickListener {
            val age = mBinding.etAge.text.trim().toString().toIntOrDefault(0)
            if (age <= 0) {
                requireContext().showToast("请先填写您的年龄")
                return@setOnClickListener
            }

        }
        mBinding.btnConfirm.setOnClickListener {
            onSelected?.invoke(1)
            dismiss()
        }
        return mBinding.root
    }

    override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
        // 宽高
        resources.displayMetrics?.widthPixels?.let {
            layoutParams.width = (it * 0.5).toInt() - 1
        }
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 位置
        layoutParams.gravity = Gravity.END
        // 透明度
        layoutParams.dimAmount = 0.6f
    }

}
