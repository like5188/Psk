package com.psk.shangxiazhi.selectdevice

import android.annotation.SuppressLint
import android.content.DialogInterface
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
import com.psk.device.ScanManager
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.databinding.DialogFragmentScanDeviceBinding
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 扫描指定设备类型的设备
 */
class ScanDeviceDialogFragment private constructor() : BaseDialogFragment() {
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
    var onSelected: ((BleScanInfo) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_scan_device, container, true)
        mBinding.rv.layoutManager = WrapLinearLayoutManager(requireContext())
        mBinding.rv.adapter = mAdapter
        mAdapter.addOnItemClickListener {
            onSelected?.invoke(it.binding.bleScanInfo!!)
            dismiss()
        }
        return mBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        stopScan()
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
            ScanManager.startScan(deviceType)
                .onStart {
                    mBinding.tvTitle.text = "(${deviceType.des}) 扫描中……"
                }.onCompletion {
                    mBinding.tvTitle.text = "(${deviceType.des}) 扫描完成"
                }.collect {
                    val name = it.device.name
                    val address = it.device.address
                    if (mAdapter.currentList.firstOrNull { it?.address == address } == null) {// 防止重复添加
                        val newItems = mAdapter.currentList.toMutableList()
                        newItems.add(BleScanInfo(name, address))
                        mAdapter.submitList(newItems)
                    }
                }
        }
    }

    private fun stopScan() {
        ScanManager.stopScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScanManager.close()
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
