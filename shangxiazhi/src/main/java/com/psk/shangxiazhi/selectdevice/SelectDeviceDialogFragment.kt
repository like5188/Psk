package com.psk.shangxiazhi.selectdevice

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.like.common.base.BaseDialogFragment
import com.like.common.util.gone
import com.like.common.util.showToast
import com.like.common.util.visible
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
        private const val KEY_SELECTED_DEVICE_MAP = "key_selected_device_map"
        fun newInstance(selectedDeviceMap: Map<DeviceType, BleScanInfo>?): SelectDeviceDialogFragment {
            return SelectDeviceDialogFragment().apply {
                arguments = bundleOf(
                    KEY_SELECTED_DEVICE_MAP to selectedDeviceMap
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentSelectDeviceBinding
    var onSelected: ((deviceMap: Map<DeviceType, BleScanInfo>) -> Unit)? = null
    private val deviceTypes = arrayOf(
        DeviceType.ShangXiaZhi,
        DeviceType.BloodOxygen,
        DeviceType.BloodPressure,
        DeviceType.HeartRate,
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val selectedDeviceMap =
            (arguments?.getSerializable(KEY_SELECTED_DEVICE_MAP) as? Map<DeviceType, BleScanInfo>)?.toMutableMap() ?: mutableMapOf()
        deviceTypes.forEach { deviceType ->
            val binding = DataBindingUtil.inflate<ViewSelectDeviceBinding>(
                inflater, R.layout.view_select_device, mBinding.llContainer, true
            )
            binding.ivClose.setOnClickListener {
                binding.tvName.text = ""
                selectedDeviceMap.remove(deviceType)
            }
            binding.tvName.doAfterTextChanged {
                if (!it.isNullOrEmpty()) {
                    binding.ivClose.visible()
                } else {
                    binding.ivClose.gone()
                }
            }
            binding.tvDeviceTypeDes.text = deviceType.des
            binding.tvName.text = selectedDeviceMap[deviceType]?.name ?: ""
            binding.cardView.setOnClickListener {
                ScanDeviceDialogFragment.newInstance(deviceType).apply {
                    onSelected = { bleSanInfo ->
                        selectedDeviceMap[deviceType] = bleSanInfo
                        binding.tvName.text = bleSanInfo.name
                    }
                }.show(requireActivity())
            }
        }
        mBinding.btnConfirm.setOnClickListener {
//            if (!selectedDeviceMap.containsKey(DeviceType.ShangXiaZhi)) {
//                context?.showToast("请先选择上下肢设备")
//            } else {
                onSelected?.invoke(selectedDeviceMap)
                dismiss()
//            }
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
