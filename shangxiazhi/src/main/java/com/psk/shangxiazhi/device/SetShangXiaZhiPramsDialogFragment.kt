package com.psk.shangxiazhi.device

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentSetShangXiaZhiParamsBinding
import com.psk.shangxiazhi.util.LevelView

class SetShangXiaZhiPramsDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        fun newInstance(): SetShangXiaZhiPramsDialogFragment {
            return SetShangXiaZhiPramsDialogFragment()
        }
    }

    private lateinit var mBinding: DialogFragmentSetShangXiaZhiParamsBinding
    var onSelected: ((BleScanInfo) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_set_shang_xia_zhi_params, container, true)
        mBinding.levelViewSpeed.onChangeListener = object : LevelView.OnChangeListener {
            override fun onAdd(level: Int, number: Int): Boolean {
                println("onAdd level=$level number=$number number/5=${number / 5}")
                return number / 5 > level
            }

            override fun onMinus(level: Int, number: Int): Boolean {
                println("onMinus level=$level number=$number number/5=${number / 5}")
                return number / 5 < level
            }
        }
        mBinding.levelViewSpasm.onChangeListener = object : LevelView.OnChangeListener {
            override fun onAdd(level: Int, number: Int): Boolean {
                return number > level
            }

            override fun onMinus(level: Int, number: Int): Boolean {
                return number < level
            }
        }
        mBinding.levelViewTime.onChangeListener = object : LevelView.OnChangeListener {
            override fun onAdd(level: Int, number: Int): Boolean {
                return number / 5 > level
            }

            override fun onMinus(level: Int, number: Int): Boolean {
                return number / 5 < level
            }
        }
        mBinding.levelViewResistance.onChangeListener = object : LevelView.OnChangeListener {
            override fun onAdd(level: Int, number: Int): Boolean {
                return number > level
            }

            override fun onMinus(level: Int, number: Int): Boolean {
                return number < level
            }
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
