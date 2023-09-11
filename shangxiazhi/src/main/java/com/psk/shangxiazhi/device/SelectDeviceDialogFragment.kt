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
import com.like.common.util.visible
import com.psk.ble.DeviceType
import com.psk.common.util.showToast
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentSelectDeviceBinding
import com.psk.shangxiazhi.databinding.ViewSelectDeviceBinding

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
    var onSelected: ((deviceMap: Map<DeviceType, BleScanInfo>, shangXiaZhiParams: ShangXiaZhiParams?) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val selectDeviceMap = mutableMapOf<DeviceType, BleScanInfo>()
        var shangXiaZhiParams: ShangXiaZhiParams? = null
        val deviceTypes = arguments?.getSerializable(KEY_DEVICE_TYPES) as? Array<DeviceType>
        deviceTypes?.forEach { deviceType ->
            val binding = DataBindingUtil.inflate<ViewSelectDeviceBinding>(
                inflater,
                R.layout.view_select_device,
                mBinding.llContainer,
                true
            )
            binding.tvDeviceTypeDes.text = deviceType.des
            binding.ll.setOnClickListener {
                ScanDeviceDialogFragment.newInstance(deviceType).apply {
                    onSelected = {
                        selectDeviceMap[deviceType] = it
                        binding.tvName.text = it.name
                        if (deviceType == DeviceType.ShangXiaZhi) {
                            binding.tvFun.visible()
                            binding.tvFun.setOnClickListener {
                                SetShangXiaZhiPramsDialogFragment.newInstance().apply {
                                    onSelected = {
                                        shangXiaZhiParams = it
                                        binding.tvFun.text = "修改参数 >"
                                    }
                                }.show(this@SelectDeviceDialogFragment)
                            }
                        }
                    }
                }.show(requireActivity())
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            if (selectDeviceMap.isEmpty()) {
                context?.showToast("请先选择设备")
            } else if (selectDeviceMap.containsKey(DeviceType.ShangXiaZhi) && shangXiaZhiParams == null) {
                context?.showToast("请先设置上下肢参数")
            } else {
                onSelected?.invoke(selectDeviceMap, shangXiaZhiParams)
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
