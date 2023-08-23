package com.psk.shangxiazhi.devices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.AutoWired
import com.like.common.util.activityresultlauncher.startActivityForResult
import com.like.common.util.injectForIntentExtras
import com.like.common.util.visible
import com.psk.device.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivitySelectDeviceBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 选择设备界面
 */
class SelectDeviceActivity : AppCompatActivity() {
    companion object {
        fun start(
            activity: ComponentActivity,
            deviceTypes: Array<DeviceType>,
            callback: ActivityResultCallback<ActivityResult>
        ) {
            activity.startActivityForResult<SelectDeviceActivity>(
                "deviceTypes" to deviceTypes,
                callback = callback
            )
        }
    }

    @AutoWired
    val deviceTypes: Array<DeviceType>? = null

    private val mBinding: ActivitySelectDeviceBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_select_device)
    }
    private val mViewModel: SelectDeviceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
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
    }

    private fun showScanDeviceDialogFragment(deviceType: DeviceType) {
        ScanDeviceDialogFragment.newInstance(deviceType).apply {
            onSelected = {
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
            show(this@SelectDeviceActivity)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
