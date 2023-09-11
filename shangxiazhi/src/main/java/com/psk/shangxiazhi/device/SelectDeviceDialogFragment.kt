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
    var onSelected: ((deviceMap: Map<DeviceType, BleScanInfo>, shangXiaZhiParams: ShangXiaZhiParams?, targetHeartRate: Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val selectDeviceMap = mutableMapOf<DeviceType, BleScanInfo>()
        var shangXiaZhiParams: ShangXiaZhiParams? = null
        var targetHeartRate = 0
        val deviceTypes = arguments?.getSerializable(KEY_DEVICE_TYPES) as? Array<DeviceType>
        deviceTypes?.forEach { deviceType ->
            val binding = DataBindingUtil.inflate<ViewSelectDeviceBinding>(
                inflater, R.layout.view_select_device, mBinding.llContainer, true
            )
            binding.tvDeviceTypeDes.text = deviceType.des
            binding.ll.setOnClickListener {
                ScanDeviceDialogFragment.newInstance(deviceType).apply {
                    onSelected = {
                        selectDeviceMap[deviceType] = it
                        binding.tvName.text = it.name
                        when (deviceType) {
                            DeviceType.ShangXiaZhi -> {
                                if (binding.tvFun.text.isEmpty()) {
                                    binding.tvFun.text = "设置参数 >"
                                }
                                binding.tvFun.visible()
                                binding.tvFun.setOnClickListener {
                                    SetShangXiaZhiPramsDialogFragment.newInstance().apply {
                                        onSelected = {
                                            shangXiaZhiParams = it
                                            binding.tvFun.text = "修改参数 >"
                                        }
                                    }.show(this@SelectDeviceDialogFragment)
                                }
                                binding.tvFun.requestFocus()
                            }

                            DeviceType.HeartRate -> {
                                // 靶心率=[(220-年龄）-静态心率]*(达到最大心率的一定百分比，通常为60%---80%)+静态心率
                                if (binding.tvFun.text.isEmpty()) {
                                    binding.tvFun.text = "测量靶心率 >"
                                }
                                binding.tvFun.visible()
                                binding.tvFun.setOnClickListener {
                                    MeasureTargetHeartRateDialogFragment.newInstance().apply {
                                        onSelected = {
                                            targetHeartRate = it
                                            binding.tvFun.text = "重新测量 >"
                                        }
                                    }.show(this@SelectDeviceDialogFragment)
                                }
                                binding.tvFun.requestFocus()
                            }

                            else -> {}
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
            } else if (selectDeviceMap.containsKey(DeviceType.HeartRate) && targetHeartRate == 0) {
                context?.showToast("请先测量靶心率")
            } else {
                onSelected?.invoke(selectDeviceMap, shangXiaZhiParams, targetHeartRate)
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
