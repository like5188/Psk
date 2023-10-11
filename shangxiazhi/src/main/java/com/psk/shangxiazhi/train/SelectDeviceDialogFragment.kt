package com.psk.shangxiazhi.train

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentSelectDeviceBinding
import com.psk.shangxiazhi.databinding.ViewSelectDeviceBinding

/**
 * 选择设备
 */
class SelectDeviceDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_DEVICE_TYPES = "key_device_types"
        fun newInstance(deviceTypes: Array<DeviceType>): SelectDeviceDialogFragment {
            return SelectDeviceDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DEVICE_TYPES to deviceTypes
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSelectDeviceBinding
    var onSelected: ((deviceMap: Map<DeviceType, BleScanInfo>) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val selectDeviceMap = mutableMapOf<DeviceType, BleScanInfo>()
        val deviceTypes = arguments?.getSerializable(KEY_DEVICE_TYPES) as? Array<DeviceType>
        deviceTypes?.forEach { deviceType ->
            val binding = DataBindingUtil.inflate<ViewSelectDeviceBinding>(
                inflater, R.layout.view_select_device, mBinding.llContainer, true
            )
            binding.tvDeviceTypeDes.text = deviceType.des
            binding.cardView.setOnClickListener {
                ScanDeviceDialogFragment.newInstance(deviceType).apply {
                    onSelected = { bleSanInfo ->
                        selectDeviceMap[deviceType] = bleSanInfo
                        binding.tvName.text = bleSanInfo.name
                    }
                }.show(requireActivity())
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            if (!selectDeviceMap.containsKey(DeviceType.ShangXiaZhi)) {
                context?.showToast("请先选择上下肢设备")
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
