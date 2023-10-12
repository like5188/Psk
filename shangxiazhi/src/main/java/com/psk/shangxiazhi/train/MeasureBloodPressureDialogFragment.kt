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
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BloodPressureRepository
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.DialogFragmentMeasureBloodPressureBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

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

    private val repository = get<DeviceManager>().createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private lateinit var mBinding: DialogFragmentMeasureBloodPressureBinding
    var onSelected: ((BloodPressure) -> Unit)? = null
    private var job: Job? = null
    private var bloodPressure: BloodPressure? = null
    private val mProgressDialog by lazy {
        ProgressDialog(requireContext(), "测量中，请稍后……")
    }

    private fun startJob() {
        if (job != null) {
            context?.showToast("正在测量，请稍后")
            return
        }
        job = lifecycleScope.launch(Dispatchers.Main) {
            mProgressDialog.show()
            bloodPressure = repository.measure()
            cancelJob()
            println("血压：$bloodPressure")
            mBinding.tvSbp.text = bloodPressure?.sbp?.toString() ?: ""
            mBinding.tvDbp.text = bloodPressure?.dbp?.toString() ?: ""
            mProgressDialog.dismiss()
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
        repository.enable(arguments?.getString(KEY_DEVICE_NAME) ?: "", arguments?.getString(KEY_DEVICE_ADDRESS) ?: "")
        mBinding.btnMeasure.setOnClickListener {
            if (repository.isConnected()) {
                startJob()
            } else {
                repository.connect(lifecycleScope, {
                    context?.showToast("血压仪连接成功，开始测量")
                    startJob()
                }) {
                    context?.showToast("血压仪连接失败")
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
