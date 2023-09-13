package com.psk.shangxiazhi.train

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentSetShangXiaZhiParamsBinding

/**
 * 设置上下肢康复机参数
 */
class SetShangXiaZhiPramsDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        fun newInstance(): SetShangXiaZhiPramsDialogFragment {
            return SetShangXiaZhiPramsDialogFragment()
        }
    }

    private lateinit var mBinding: DialogFragmentSetShangXiaZhiParamsBinding
    var onSelected: ((ShangXiaZhiParams) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_set_shang_xia_zhi_params, container, true)
        mBinding.btnConfirm.setOnClickListener {
            val passiveModule: Boolean = mBinding.rgPassiveMode.checkedRadioButtonId == R.id.rbPassive
            val intelligent: Boolean = mBinding.switchSmartMode.isChecked
            val turn2: Boolean = mBinding.rgOrientation.checkedRadioButtonId == R.id.rbForward
            val shangXiaZhiParams = if (passiveModule) {
                val time: Int = mBinding.levelViewTime.getNumber()
                val speedLevel: Int = mBinding.levelViewSpeed.getLevel()
                val spasmLevel: Int = mBinding.levelViewSpasm.getLevel()
                ShangXiaZhiParams(
                    passiveModule = passiveModule,
                    time = time,
                    speedLevel = speedLevel,
                    spasmLevel = spasmLevel,
                    resistanceLevel = 0,
                    intelligent = intelligent,
                    forward = turn2
                )
            } else {
                val resistanceLevel: Int = mBinding.levelViewResistance.getLevel()
                ShangXiaZhiParams(
                    passiveModule = passiveModule,
                    time = 0,
                    speedLevel = 0,
                    spasmLevel = 0,
                    resistanceLevel = resistanceLevel,
                    intelligent = intelligent,
                    forward = turn2
                )
            }
            onSelected?.invoke(shangXiaZhiParams)
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
