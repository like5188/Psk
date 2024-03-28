package com.psk.sixminutes

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.ble.central.util.PermissionUtils
import com.like.common.base.BaseLazyFragment
import com.like.common.util.Logger
import com.like.common.util.dp
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.ecg.effect.CirclePathEffect
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.DynamicDataPainter
import com.psk.sixminutes.databinding.FragmentDevicesBinding
import kotlinx.coroutines.launch

class DevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_ID = "key_id"
        private const val KEY_DEVICES = "key_devices"

        /**
         * @param id        此次运动唯一id。
         * @param devices   设备信息。key:设备类型，value:设备名称和地址。
         */
        fun newInstance(
            id: Long,
            devices: Map<DeviceType, Pair<String, String>>
        ): DevicesFragment {
            return DevicesFragment().apply {
                arguments = bundleOf(
                    KEY_ID to id,
                    KEY_DEVICES to devices
                )
            }
        }
    }

    private lateinit var mBinding: FragmentDevicesBinding
    private val heartRateBusinessManager by lazy {
        HeartRateBusinessManager()
    }
    private val bloodOxygenBusinessManager by lazy {
        BloodOxygenBusinessManager()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        mBinding.tvEcgParams.text = "25 mm/s    10mm/mV"
        mBinding.ecgView.apply {
            setGridSize(10f.dp)
            setBgPainter(BgPainter(Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 2f
                isAntiAlias = true
                alpha = 120
            }, Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 1f
                isAntiAlias = true
                pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
                alpha = 90
            }, null, Paint().apply {
                textSize = 18f
                color = Color.RED
            }))
            setDataPainters(
                listOf(
                    DynamicDataPainter(CirclePathEffect(), Paint().apply {
                        color = Color.parseColor("#44C71E")
                        strokeWidth = 3f
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    })
                )
            )
            setLeadsNames(listOf("I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"))
        }
        return mBinding.root
    }

    override fun onLazyLoadData() {
        val id = arguments?.getLong(KEY_ID)
        val devices = (arguments?.getSerializable(KEY_DEVICES) as? Map<DeviceType, Pair<String, String>>)
        if (id == null || devices.isNullOrEmpty()) {
            return
        }
        Logger.i("id=$id")
        lifecycleScope.launch {
            if (!PermissionUtils.requestConnectEnvironment(requireActivity())) {
                return@launch
            }
            DeviceRepositoryManager.init(requireContext())
            devices.forEach {
                val deviceType = it.key
                val name = it.value.first
                val address = it.value.second
                Logger.i("deviceType=$deviceType, name=$name, address=$address")
                when (deviceType) {
                    DeviceType.HeartRate -> {
                        heartRateBusinessManager.init(requireContext(), name, address)
                        mBinding.ecgView.setSampleRate(heartRateBusinessManager.getSampleRate())
                        heartRateBusinessManager.connect(requireContext(), id, lifecycleScope, onHeartRateResult = {
                            mBinding.tvHeartRate.text = it.toString()
                        }) {
                            mBinding.ecgView.addData(it)
                        }
                    }

                    DeviceType.BloodOxygen -> {
                        bloodOxygenBusinessManager.init(requireContext(), name, address)
                        bloodOxygenBusinessManager.connect(requireContext(), id, lifecycleScope) {
                            mBinding.tvBloodOxygen.text = it.toString()
                        }
                    }

                    else -> {
                        Logger.e("不支持的设备类型: $deviceType")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartRateBusinessManager.disconnect()
    }

}
