package com.psk.shangxiazhi.devices

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
import com.psk.common.util.showToast
import com.psk.device.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentSelectDeviceBinding
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@OptIn(KoinApiExtension::class)
class SelectDeviceDialogFragment private constructor() : BaseDialogFragment(), KoinComponent {
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
    private val selectDeviceMap = mutableMapOf<DeviceType, BleScanInfo>()
    var onSelected: ((Map<DeviceType, BleScanInfo>) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_select_device, container, true)
        val deviceTypes = arguments?.getSerializable(KEY_DEVICE_TYPES) as? Array<DeviceType>
        mBinding.btnConfirm.setOnClickListener {
            if (deviceTypes?.size != selectDeviceMap.size) {
                context?.showToast("请先选择所有设备")
                return@setOnClickListener
            }
            onSelected?.invoke(selectDeviceMap)
            dismiss()
        }
        deviceTypes?.forEach { deviceType ->
            when (deviceType) {
                DeviceType.BloodOxygen -> {
                    mBinding.llBloodOxygen.visible()
                    mBinding.llBloodOxygen.setOnClickListener {
                        showScanDeviceDialogFragment(deviceType)
                    }
                }

                DeviceType.BloodPressure -> {
                    mBinding.llBloodPressure.visible()
                    mBinding.llBloodPressure.setOnClickListener {
                        showScanDeviceDialogFragment(deviceType)
                    }
                }

                DeviceType.HeartRate -> {
                    mBinding.llHeartRate.visible()
                    mBinding.llHeartRate.setOnClickListener {
                        showScanDeviceDialogFragment(deviceType)
                    }
                }

                DeviceType.ShangXiaZhi -> {
                    mBinding.llShangXiaZhi.visible()
                    mBinding.llShangXiaZhi.setOnClickListener {
                        showScanDeviceDialogFragment(deviceType)
                    }
                }
            }
        }
        return mBinding.root
    }

    private fun showScanDeviceDialogFragment(deviceType: DeviceType) {
        ScanDeviceDialogFragment.newInstance(deviceType).apply {
            onSelected = {
                selectDeviceMap[deviceType] = it
                when (deviceType) {
                    DeviceType.BloodOxygen -> {
                        mBinding.tvBloodOxygenName.text = it.name
                    }

                    DeviceType.BloodPressure -> {
                        mBinding.tvBloodPressureName.text = it.name
                    }

                    DeviceType.HeartRate -> {
                        mBinding.tvHeartRateName.text = it.name
                    }

                    DeviceType.ShangXiaZhi -> {
                        mBinding.tvShangXiaZhiName.text = it.name
                    }
                }
            }
            show(this@SelectDeviceDialogFragment)
        }
    }

    override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
        // 宽高
        resources.displayMetrics?.widthPixels?.let {
            layoutParams.width = (it * 0.5).toInt()
        }
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 位置
        layoutParams.gravity = Gravity.START
        // 透明度
        layoutParams.dimAmount = 0.6f
    }

}
