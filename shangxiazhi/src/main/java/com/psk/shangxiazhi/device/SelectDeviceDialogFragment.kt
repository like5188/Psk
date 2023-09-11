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
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentSelectDeviceBinding
import com.psk.shangxiazhi.databinding.ViewSelectDeviceBinding

class SelectDeviceDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_DATA = "key_data"
        fun newInstance(deviceTypes: Array<DeviceType>): SelectDeviceDialogFragment {
            return SelectDeviceDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DATA to deviceTypes
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSelectDeviceBinding
    var onSelected: ((Map<DeviceType, BleScanInfo>) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val selectDeviceMap = mutableMapOf<DeviceType, BleScanInfo>()
        val deviceTypes = arguments?.getSerializable(KEY_DATA) as? Array<DeviceType>
        deviceTypes?.forEach { deviceType ->
            val binding = DataBindingUtil.inflate<ViewSelectDeviceBinding>(
                inflater,
                R.layout.view_select_device,
                mBinding.llContainer,
                true
            )
            binding.tvDeviceTypeDes.text = deviceType.des
            binding.root.setOnClickListener {
//                ScanDeviceDialogFragment.newInstance(deviceType).apply {
//                    onSelected = {
//                        selectDeviceMap[deviceType] = it
//                        binding.tvName.text = it.name
//                    }
//                    show(this@SelectDeviceDialogFragment)
//                }
                SetShangXiaZhiPramsDialogFragment.newInstance().show(requireActivity())
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            if (selectDeviceMap.isEmpty()) {
                context?.showToast("请先选择设备")
            } else if (selectDeviceMap.containsKey(DeviceType.ShangXiaZhi)) {

            } else {
                onSelected?.invoke(selectDeviceMap)
                dismiss()
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
        layoutParams.gravity = Gravity.START
        // 透明度
        layoutParams.dimAmount = 0.6f
    }

}
