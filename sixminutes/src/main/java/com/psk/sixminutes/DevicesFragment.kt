package com.psk.sixminutes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.base.BaseLazyFragment
import com.like.common.util.Logger
import com.like.common.util.dp
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.business.MultiBusinessManager
import com.psk.sixminutes.databinding.FragmentDevicesBinding
import com.psk.sixminutes.model.BleInfo
import com.psk.sixminutes.model.Info
import com.psk.sixminutes.model.SocketInfo
import com.psk.sixminutes.util.createBgPainter
import com.psk.sixminutes.util.createDynamicDataPainter
import kotlinx.coroutines.launch

class DevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_ID = "key_id"
        private const val KEY_DEVICES = "key_devices"

        /**
         * @param id        此次运动唯一id。
         * @param devices   设备信息
         */
        fun newInstance(id: Long, devices: List<Info>): DevicesFragment {
            return DevicesFragment().apply {
                arguments = bundleOf(
                    KEY_ID to id,
                    KEY_DEVICES to devices
                )
            }
        }
    }

    private lateinit var mBinding: FragmentDevicesBinding
    private val multiBusinessManager by lazy {
        MultiBusinessManager()
    }
    private val params = "25 mm/s    10mm/mV"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        mBinding.tvEcgParams.text = params
        mBinding.ecgView.apply {
            setGridSize(10f.dp)
            setBgPainter(createBgPainter())
        }
        mBinding.btnBloodPressureBefore.setOnClickListener {
            if (mBinding.btnBloodPressureBefore.text == "开始测量") {
                lifecycleScope.launch {
                    multiBusinessManager.bleBloodPressureBusinessManager.measure(requireContext(), lifecycleScope) { sbp, dbp ->
                        mBinding.tvBloodPressureBeforeSbp.text = sbp.toString()
                        mBinding.tvBloodPressureBeforeDbp.text = dbp.toString()
                    }
                }
            } else {
                lifecycleScope.launch {
                    multiBusinessManager.bleBloodPressureBusinessManager.stopMeasure()
                }
            }
        }
        mBinding.btnBloodPressureAfter.setOnClickListener {
            if (mBinding.btnBloodPressureAfter.text == "开始测量") {
                lifecycleScope.launch {
                    multiBusinessManager.bleBloodPressureBusinessManager.measure(requireContext(), lifecycleScope) { sbp, dbp ->
                        mBinding.tvBloodPressureAfterSbp.text = sbp.toString()
                        mBinding.tvBloodPressureAfterDbp.text = dbp.toString()
                    }
                }
            } else {
                lifecycleScope.launch {
                    multiBusinessManager.bleBloodPressureBusinessManager.stopMeasure()
                }
            }
        }
        return mBinding.root
    }

    override fun onLazyLoadData() {
        val id = arguments?.getLong(KEY_ID)
        val devices = arguments?.getSerializable(KEY_DEVICES) as? List<Info>
        if (id == null || devices.isNullOrEmpty()) {
            return
        }
        Logger.i("id=$id")
        lifecycleScope.launch {
            multiBusinessManager.init(requireActivity(), devices)
            devices.forEach {
                when (it) {
                    is BleInfo -> {
                        when (it.deviceType) {
                            DeviceType.HeartRate -> {
                                mBinding.ecgView.setDataPainters(listOf(createDynamicDataPainter()))
                                mBinding.ecgView.setLeadsNames(listOf("I"))
                                mBinding.ecgView.setSampleRate(multiBusinessManager.bleHeartRateBusinessManager.getSampleRate())
                                multiBusinessManager.bleHeartRateBusinessManager.connect(
                                    requireContext(),
                                    id,
                                    lifecycleScope,
                                    onHeartRateResult = {
                                        mBinding.tvHeartRate.text = it.toString()
                                    }) {
                                    mBinding.ecgView.addData(it)
                                }
                            }

                            DeviceType.BloodOxygen -> {
                                multiBusinessManager.bleBloodOxygenBusinessManager.connect(requireContext(), id, lifecycleScope) {
                                    mBinding.tvBloodOxygen.text = it.toString()
                                }
                            }

                            DeviceType.BloodPressure -> {
                            }

                            else -> {
                            }
                        }
                    }

                    is SocketInfo -> {
                        when (it.deviceType) {
                            DeviceType.HeartRate -> {
                                val sampleRate = multiBusinessManager.socketHeartRateBusinessManager.getSampleRate()
                                val leadsNames = listOf(
                                    "I",
                                    "II",
                                    "III",
                                    "aVR",
                                    "aVL",
                                    "aVF",
                                    "V1",
                                    "V2",
                                    "V3",
                                    "V4",
                                    "V5",
                                    "V6"
                                )
                                var leadsIndex = 0
                                var singleEcgDialogFragment: SingleEcgDialogFragment? = null
                                mBinding.ecgView.setDataPainters((0 until 12).map { createDynamicDataPainter() }) {
                                    leadsIndex = it
                                    singleEcgDialogFragment = SingleEcgDialogFragment.newInstance(
                                        sampleRate, leadsNames[it], params
                                    ).apply {
                                        show(this@DevicesFragment)
                                    }
                                }
                                mBinding.ecgView.setLeadsNames(leadsNames)
                                mBinding.ecgView.setSampleRate(sampleRate)
                                multiBusinessManager.socketHeartRateBusinessManager.start(
                                    requireContext(),
                                    id,
                                    lifecycleScope,
                                    onHeartRateResult = {
                                        mBinding.tvHeartRate.text = it.toString()
                                    }) {
                                    mBinding.ecgView.addData(it)
                                    singleEcgDialogFragment?.addData(it[leadsIndex])
                                }
                            }

                            else -> {
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiBusinessManager.destroy()
    }

}
