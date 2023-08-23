package com.psk.shangxiazhi.devices

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.base.BaseDialogFragment
import com.like.recyclerview.layoutmanager.WrapLinearLayoutManager
import com.psk.device.BleManager
import com.psk.device.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentScanDeviceBinding
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
class ScanDeviceDialogFragment private constructor() : BaseDialogFragment(), KoinComponent {
    companion object {
        private const val KEY_DEVICE_TYPE = "key_device_type"
        fun newInstance(deviceType: DeviceType): ScanDeviceDialogFragment {
            return ScanDeviceDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DEVICE_TYPE to deviceType
                )
            }
        }
    }

    private lateinit var mBinding: DialogFragmentScanDeviceBinding
    private val mAdapter: ScanDeviceAdapter by lazy { ScanDeviceAdapter() }
    private val bleManager by inject<BleManager>()
    var onSelected: ((BleScanInfo) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_scan_device, container, true)
        mBinding.rv.layoutManager = WrapLinearLayoutManager(requireContext())
        mBinding.rv.adapter = mAdapter
        mAdapter.addOnItemClickListener {
            stopScan()
            onSelected?.invoke(it.binding.bleScanInfo!!)
            dismiss()
        }
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        (arguments?.getSerializable(KEY_DEVICE_TYPE) as? DeviceType)?.apply {
            startScan(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan(deviceType: DeviceType) {
        lifecycleScope.launch {
            bleManager.scan().collect {
                val name = it.device.name
                val address = it.device.address
                val isRightDevice = when (deviceType) {
                    DeviceType.BloodOxygen -> name?.startsWith("O2") == true
                    DeviceType.BloodPressure -> name?.startsWith("BP") == true
                    DeviceType.HeartRate -> name?.startsWith("ER1") == true
                    DeviceType.ShangXiaZhi -> name?.startsWith("RKF") == true
                }
                if (!address.isNullOrEmpty() && isRightDevice) {
                    mAdapter.submitList(listOf(BleScanInfo(name, address)))
                }
            }
        }
    }

    private fun stopScan() {
        try {
            bleManager.stopScan()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        bleManager.onDestroy()
        super.onDestroy()
    }

    override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
        // 宽高
        resources.displayMetrics?.widthPixels?.let {
            layoutParams.width = (it * 0.5).toInt()
        }
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 位置
        layoutParams.gravity = Gravity.END
        // 透明度
        layoutParams.dimAmount = 0.6f
    }

}
