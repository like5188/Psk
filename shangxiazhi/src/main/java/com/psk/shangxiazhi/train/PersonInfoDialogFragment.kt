package com.psk.shangxiazhi.train

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
import com.psk.shangxiazhi.databinding.DialogFragmentPersonInfoBinding

/**
 * 基本信息
 */
class PersonInfoDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        fun newInstance(): PersonInfoDialogFragment {
            return PersonInfoDialogFragment()
        }
    }

    private lateinit var mBinding: DialogFragmentPersonInfoBinding
    var onSelected: ((age: Int, weight: Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_person_info, container, true)
        mBinding.btnConfirm.setOnClickListener {
            val age = mBinding.etAge.text.trim().toString().toIntOrDefault(0)
            val weight = mBinding.etWeight.text.trim().toString().toIntOrDefault(0)
            if (age <= 0) {
                requireContext().showToast("请输入您的年龄")
                return@setOnClickListener
            }
            if (weight <= 0) {
                requireContext().showToast("请输入您的体重")
                return@setOnClickListener
            }
            onSelected?.invoke(age, weight)
            dismiss()
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
        layoutParams.dimAmount = 0.6f
    }

}
