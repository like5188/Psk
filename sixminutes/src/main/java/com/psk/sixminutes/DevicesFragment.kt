package com.psk.sixminutes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.like.common.base.BaseLazyFragment
import com.like.common.util.dp
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
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
        private const val KEY_NAME = "key_name"
        private const val KEY_AGE = "key_age"
        private const val KEY_SEX = "key_sex"
        private const val KEY_HEIGHT = "key_height"
        private const val KEY_WEIGHT = "key_weight"

        /**
         * @param id        此次运动唯一id。
         * @param devices   设备信息
         */
        fun newInstance(
            id: Long,
            devices: List<Info>,
            name: String,
            age: String,
            sex: String,
            height: String,
            weight: String
        ): DevicesFragment {
            return DevicesFragment().apply {
                arguments = bundleOf(
                    KEY_ID to id,
                    KEY_DEVICES to devices,
                    KEY_NAME to name,
                    KEY_AGE to age,
                    KEY_SEX to sex,
                    KEY_HEIGHT to height,
                    KEY_WEIGHT to weight
                )
            }
        }
    }

    private val mViewModel: DevicesViewModel by lazy {
        ViewModelProvider(this).get(DevicesViewModel::class.java)
    }
    private lateinit var mBinding: FragmentDevicesBinding
    private val params = "25 mm/s    10mm/mV"
    private var id: Long = 0L
    private var devices: List<Info>? = null
    private var singleEcgDialogFragment: SingleEcgDialogFragment? = null
    private var leadsIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        arguments?.let {
            id = it.getLong(KEY_ID)
            devices = it.getSerializable(KEY_DEVICES) as? List<Info>
            devices?.forEach {
                when (it) {
                    is BleInfo -> {
                        when (it.deviceType) {
                            DeviceType.HeartRate -> {
                                mBinding.tvHeartRateName.text = "(${it.name})"
                            }

                            DeviceType.BloodOxygen -> {
                                mBinding.tvBloodOxygenName.text = "(${it.name})"
                                mBinding.btnBloodPressureBefore.setOnClickListener {
                                    mViewModel.measureBloodPressureBefore()
                                }
                                mBinding.btnBloodPressureAfter.setOnClickListener {
                                    mViewModel.measureBloodPressureAfter()
                                }
                            }

                            DeviceType.BloodPressure -> {
                                mBinding.tvBloodPressureName.text = "(${it.name})"
                            }

                            else -> {}
                        }
                    }

                    is SocketInfo -> {
                        when (it.deviceType) {
                            DeviceType.HeartRate -> {
                                mBinding.tvHeartRateName.text = "(${it.name})"
                            }

                            else -> {}
                        }

                    }
                }
            }
            mBinding.tvName.text = it.getString(KEY_NAME, "")
            mBinding.tvAge.text = it.getString(KEY_AGE)
            mBinding.tvSex.text = it.getString(KEY_SEX, "")
            mBinding.tvHeight.text = it.getString(KEY_HEIGHT)
            mBinding.tvWeight.text = it.getString(KEY_WEIGHT)
        }
        if (id == 0L || devices.isNullOrEmpty()) {
            return null
        }
        mBinding.tvEcgParams.text = params
        mBinding.ecgView.apply {
            setGridSize(10f.dp)
            setBgPainter(createBgPainter())
        }
        mBinding.btnStart.setOnClickListener {
            if (mBinding.btnStart.text == "开始") {
                mBinding.btnStart.text = "紧急停止"
                mViewModel.startTimer()
                mViewModel.connect(id, devices)
            } else {
                requireActivity().finish()
            }
        }
        return mBinding.root
    }

    override fun onLazyLoadData() {
        lifecycleScope.launch {
            mViewModel.init(requireActivity(), devices)
            initEcgView()
            collectUiState()
        }
    }

    private fun initEcgView() {
        devices?.forEach {
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            mBinding.ecgView.setDataPainters(listOf(createDynamicDataPainter()))
                            mBinding.ecgView.setLeadsNames(listOf("I"))
                            mBinding.ecgView.setSampleRate(mViewModel.getSampleRate(it))
                        }

                        else -> {}
                    }
                }

                is SocketInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            val sampleRate = mViewModel.getSampleRate(it)
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
                        }

                        else -> {}
                    }

                }
            }
        }
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(DevicesUiState::date) {
                mBinding.tvDate.text = it
            }
            collectDistinctProperty(DevicesUiState::time) {
                mBinding.tvTime.text = it
            }
            collectDistinctProperty(DevicesUiState::finish) {
                if (it) {
                    requireContext().showToast("运动完毕")
                }
            }
            collectDistinctProperty(DevicesUiState::sbpBefore) {
                mBinding.tvBloodPressureBeforeSbp.text = it
            }
            collectDistinctProperty(DevicesUiState::dbpBefore) {
                mBinding.tvBloodPressureBeforeDbp.text = it
            }
            collectDistinctProperty(DevicesUiState::sbpAfter) {
                mBinding.tvBloodPressureAfterSbp.text = it
            }
            collectDistinctProperty(DevicesUiState::dbpAfter) {
                mBinding.tvBloodPressureAfterDbp.text = it
            }
            collectDistinctProperty(DevicesUiState::heartRateStatus) {
                mBinding.tvHeartRateStatus.text = it
                when (it) {
                    "已连接" -> {
                        mBinding.tvHeartRateStatus.setTextColor(Color.GREEN)
                    }

                    "已断开" -> {
                        mBinding.tvHeartRateStatus.setTextColor(Color.RED)
                    }
                }
            }
            collectDistinctProperty(DevicesUiState::heartRate) {
                mBinding.tvHeartRate.text = it
            }
            collectProperty(DevicesUiState::ecgDatas) {
                it?.let {
                    mBinding.ecgView.addData(it)
                    singleEcgDialogFragment?.addData(it[leadsIndex])
                }
            }
            collectDistinctProperty(DevicesUiState::bloodOxygenStatus) {
                mBinding.tvBloodOxygenStatus.text = it
                when (it) {
                    "已连接" -> {
                        mBinding.tvBloodOxygenStatus.setTextColor(Color.GREEN)
                    }

                    "已断开" -> {
                        mBinding.tvBloodOxygenStatus.setTextColor(Color.RED)
                    }
                }
            }
            collectDistinctProperty(DevicesUiState::bloodOxygen) {
                mBinding.tvBloodOxygen.text = it
            }
            collectDistinctProperty(DevicesUiState::lapStatus) {
                mBinding.tvLapStatus.text = it
            }
            collectDistinctProperty(DevicesUiState::lapMeters) {
                mBinding.tvLapMeters.text = it
            }
            collectDistinctProperty(DevicesUiState::lapCount) {
                mBinding.tvLapCount.text = it
            }
            collectNotHandledEventProperty(DevicesUiState::toastEvent) {
                requireContext().showToast(toastEvent = it)
            }
        }
    }

}
