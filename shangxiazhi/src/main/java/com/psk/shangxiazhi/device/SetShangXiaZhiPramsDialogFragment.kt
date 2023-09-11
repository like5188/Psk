package com.psk.shangxiazhi.device

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
                    resistance = 0,
                    intelligent = intelligent,
                    turn2 = turn2
                )
            } else {
                val resistance: Int = mBinding.levelViewResistance.getLevel()
                ShangXiaZhiParams(
                    passiveModule = passiveModule,
                    time = 0,
                    speedLevel = 0,
                    spasmLevel = 0,
                    resistance = resistance,
                    intelligent = intelligent,
                    turn2 = turn2
                )
            }
            onSelected?.invoke(shangXiaZhiParams)
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
