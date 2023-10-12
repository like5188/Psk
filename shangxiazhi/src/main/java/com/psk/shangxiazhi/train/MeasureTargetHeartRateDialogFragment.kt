package com.psk.shangxiazhi.train

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
import com.like.common.util.showToast
import com.psk.common.customview.ProgressDialog
import com.psk.device.DeviceManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentMeasureTargetHeartRateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

/**
 * 测量靶心率
 */
class MeasureTargetHeartRateDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_AGE = "key_age"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_DEVICE_ADDRESS = "key_device_address"
        fun newInstance(age: Int, deviceName: String, deviceAddress: String): MeasureTargetHeartRateDialogFragment {
            return MeasureTargetHeartRateDialogFragment().apply {
                arguments = bundleOf(
                    KEY_AGE to age, KEY_DEVICE_NAME to deviceName, KEY_DEVICE_ADDRESS to deviceAddress
                )
            }
        }
    }

    private val repository = get<DeviceManager>().createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private lateinit var mBinding: DialogFragmentMeasureTargetHeartRateBinding
    var onSelected: ((minTargetHeartRate: Int, maxTargetHeartRate: Int) -> Unit)? = null
    private var job: Job? = null
    private val heartRates = mutableListOf<Int>()
    private val mProgressDialog by lazy {
        ProgressDialog(requireContext(), "测量需要1分钟，请稍后……")
    }

    private fun startJob() {
        if (job != null) {
            context?.showToast("正在测量，请稍后")
            return
        }
        job = lifecycleScope.launch(Dispatchers.Main) {
            heartRates.clear()
            repository.fetch().filterNotNull().map {
                it.value
            }.filter {
                it > 0
            }.onEach {
                heartRates.add(it)
            }.distinctUntilChanged().flowOn(Dispatchers.IO).collect {
                println("心率：$it")
                mBinding.tvHeartRate.text = it.toString()
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                mProgressDialog.show()
            }
            // 测量一分钟
            delay(60000)
            cancelJob()
            val targetHeartRate = calc()
            withContext(Dispatchers.Main) {
                mBinding.tvTargetHeartRate.text = "${targetHeartRate.first}~${targetHeartRate.second}"
                mProgressDialog.dismiss()
            }
        }
    }

    private fun calc(): Pair<Int, Int> {
        // 靶心率=[(220-年龄)-静态心率(一分钟平均值)]*(达到最大心率的一定百分比，通常为60%---80%)+静态心率
        if (heartRates.isEmpty()) {
            return Pair(0, 0)
        }
        val heartRate = heartRates.average().toInt()
        val age = arguments?.getInt(KEY_AGE) ?: 0
        val minTargetHeartRate = (((220 - age) - heartRate) * 0.6 + heartRate).toInt()
        val maxTargetHeartRate = (((220 - age) - heartRate) * 0.8 + heartRate).toInt()
        println("heartRates=$heartRates arv=$heartRate min=$minTargetHeartRate max=$maxTargetHeartRate")
        return Pair(minTargetHeartRate, maxTargetHeartRate)
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
        repository.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_measure_target_heart_rate, container, true)
        repository.enable(arguments?.getString(KEY_DEVICE_NAME) ?: "", arguments?.getString(KEY_DEVICE_ADDRESS) ?: "")
        mBinding.btnMeasure.setOnClickListener {
            if (repository.isConnected()) {
                startJob()
            } else {
                repository.connect(lifecycleScope, {
                    context?.showToast("心电仪连接成功，开始测量")
                    startJob()
                }) {
                    context?.showToast("心电仪连接失败")
                    cancelJob()
                }
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            val targetHeartRate = calc()
            if (targetHeartRate.first <= 0 || targetHeartRate.second <= 0) {
                context?.showToast("请先进行测量")
                return@setOnClickListener
            }
            onSelected?.invoke(targetHeartRate.first, targetHeartRate.second)
            dismiss()
        }
        return mBinding.root
    }

    override fun initLayoutParams(layoutParams: WindowManager.LayoutParams) {
        // 宽高
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        // 位置
        layoutParams.gravity = Gravity.CENTER
        // 透明度
        layoutParams.dimAmount = 0.6f
    }

}
