package com.psk.shangxiazhi.measure

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.base.BaseDialogFragment
import com.like.common.util.showToast
import com.psk.common.customview.CountDownTimerProgressDialog
import com.psk.common.customview.ProgressDialog
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BloodPressureRepository
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentMeasureBloodPressureBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 测量血压
 */
class MeasureBloodPressureDialogFragment private constructor() : BaseDialogFragment() {
    companion object {
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_DEVICE_ADDRESS = "key_device_address"
        fun newInstance(deviceName: String, deviceAddress: String): MeasureBloodPressureDialogFragment {
            return MeasureBloodPressureDialogFragment().apply {
                arguments = bundleOf(
                    KEY_DEVICE_NAME to deviceName, KEY_DEVICE_ADDRESS to deviceAddress
                )
            }
        }
    }

    private val repository = DeviceRepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private lateinit var mBinding: DialogFragmentMeasureBloodPressureBinding
    var onSelected: ((BloodPressure) -> Unit)? = null
    var onCanceled: (() -> Unit)? = null
    private var job: Job? = null
    private var bloodPressure: BloodPressure? = null
    private val mProgressDialog by lazy {
        ProgressDialog(requireContext(), "正在连接血压仪，请稍后……")
    }
    private val mCountDownTimerProgressDialog by lazy {
        CountDownTimerProgressDialog(requireContext(), "正在测量血压，请稍后！", countDownTime = 0L, onCanceled = {
            lifecycleScope.launch(Dispatchers.IO) {
                job?.cancelAndJoin()// 这里必须等待上一条命令执行完毕，否则会导致stopMeasure失败
                job = null
                delay(100)
                repository.stopMeasure()
                bloodPressure = null
                withContext(Dispatchers.Main) {
                    mBinding.tvSbp.text = ""
                    mBinding.tvDbp.text = ""
                }
            }
        })
    }

    private fun startJob() {
        if (job != null) {
            context?.showToast("正在测量，请稍后")
            return
        }
        job = lifecycleScope.launch(Dispatchers.Main) {
            mCountDownTimerProgressDialog.show()
            bloodPressure = null
            mBinding.tvSbp.text = ""
            mBinding.tvDbp.text = ""
            delay(100)// 这里延迟一下，避免刚连接成功就测量返回null值。
            bloodPressure = repository.measure()
            cancelJob()
            println("血压：$bloodPressure")
            mBinding.tvSbp.text = bloodPressure?.sbp?.toString() ?: ""
            mBinding.tvDbp.text = bloodPressure?.dbp?.toString() ?: ""
            mCountDownTimerProgressDialog.dismiss()
            if (bloodPressure == null) {
                context?.showToast("测量失败，请重新测量！")
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
        repository.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_measure_blood_pressure, container, true)
        repository.init(requireContext(), arguments?.getString(KEY_DEVICE_NAME) ?: "", arguments?.getString(KEY_DEVICE_ADDRESS) ?: "")
        mBinding.btnMeasure.setOnClickListener {
            if (repository.isConnected()) {
                startJob()
            } else {
                mProgressDialog.show()
                repository.connect(lifecycleScope, 0L, {
                    mProgressDialog.dismiss()
                    context?.showToast("血压仪连接成功，开始测量")
                    startJob()
                }) {
                    mCountDownTimerProgressDialog.dismiss()
                    mProgressDialog.dismiss()
                    context?.showToast("血压仪连接失败，无法进行测量")
                    cancelJob()
                }
            }
        }
        mBinding.btnConfirm.setOnClickListener {
            if (bloodPressure == null) {
                context?.showToast("请先进行测量")
                return@setOnClickListener
            }
            onSelected?.invoke(bloodPressure!!)
            dismiss()
        }
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                // 返回键
                onCanceled?.invoke()
            }
            false
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
