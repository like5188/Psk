package com.psk.shangxiazhi.device

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.psk.ble.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentSetShangXiaZhiParamsBinding

class SetShangXiaZhiPramsDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_DEVICE_TYPE = "key_device_type"
        fun newInstance(deviceType: DeviceType): SetShangXiaZhiPramsDialogFragment {
            return SetShangXiaZhiPramsDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DEVICE_TYPE to deviceType
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSetShangXiaZhiParamsBinding
    var onSelected: ((BleScanInfo) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_set_shang_xia_zhi_params, container, true)
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
