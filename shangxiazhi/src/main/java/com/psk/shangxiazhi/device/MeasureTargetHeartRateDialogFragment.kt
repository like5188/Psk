package com.psk.shangxiazhi.device

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
import com.like.common.util.toIntOrDefault
import com.psk.ble.BleManager
import com.psk.ble.DeviceType
import com.psk.common.util.showToast
import com.psk.device.DeviceManager
import com.psk.device.data.source.HeartRateRepository
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentMeasureTargetHeartRateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * 测量靶心率
 */
class MeasureTargetHeartRateDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_DEVICE_ADDRESS = "key_device_address"
        fun newInstance(deviceName: String, deviceAddress: String): MeasureTargetHeartRateDialogFragment {
            return MeasureTargetHeartRateDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DEVICE_NAME to deviceName,
                    KEY_DEVICE_ADDRESS to deviceAddress
                )
            }
        }
    }

    private val bleManager by inject<BleManager>()
    private val deviceManager by inject<DeviceManager>()
    private val repository: HeartRateRepository by lazy {
        deviceManager.createRepository<HeartRateRepository>(DeviceType.HeartRate).apply {
            enable(arguments?.getString(KEY_DEVICE_NAME) ?: "", arguments?.getString(KEY_DEVICE_ADDRESS) ?: "")
        }
    }
    private lateinit var mBinding: DialogFragmentMeasureTargetHeartRateBinding
    var onSelected: ((Int) -> Unit)? = null
    private var job: Job? = null

    private fun startJob() {
        if (job != null) {
            return
        }
        job = lifecycleScope.launch(Dispatchers.IO) {
            repository.getFlow(lifecycleScope, 1).filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                mBinding.tvHeartRate.text = value.toString()
            }
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        cancelJob()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_measure_target_heart_rate, container, true)
        mBinding.btnMeasure.setOnClickListener {
            val age = mBinding.etAge.text.trim().toString().toIntOrDefault(0)
            if (age <= 0) {
                requireContext().showToast("请先填写您的年龄")
                return@setOnClickListener
            }
            bleManager.connect(DeviceType.HeartRate, lifecycleScope, 3000L, {
                startJob()
            }) {
                cancelJob()
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            onSelected?.invoke(1)
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
