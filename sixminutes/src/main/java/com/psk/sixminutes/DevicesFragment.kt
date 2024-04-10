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
import com.psk.common.customview.ProgressDialog
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.data.model.BleInfo
import com.psk.sixminutes.data.model.Info
import com.psk.sixminutes.data.model.SocketInfo
import com.psk.sixminutes.databinding.FragmentDevicesBinding
import com.psk.sixminutes.util.createBgPainter
import com.psk.sixminutes.util.createDynamicDataPainter
import kotlinx.coroutines.launch

class DevicesFragment : BaseLazyFragment() {
    companion object {
        private const val KEY_ORDER_ID = "key_order_id"
        private const val KEY_DEVICES = "key_devices"
        private const val KEY_NAME = "key_name"
        private const val KEY_AGE = "key_age"
        private const val KEY_SEX = "key_sex"
        private const val KEY_HEIGHT = "key_height"
        private const val KEY_WEIGHT = "key_weight"

        /**
         * @param orderId   此次运动唯一id。
         * @param devices   设备信息
         */
        fun newInstance(
            orderId: Long, devices: List<Info>, name: String, age: String, sex: String, height: String, weight: String
        ): DevicesFragment {
            return DevicesFragment().apply {
                arguments = bundleOf(
                    KEY_ORDER_ID to orderId,
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
    private var mm_per_s = 25
    private var mm_per_mv = 10
    private var orderId: Long = 0L
    private var devices: List<Info>? = null
    private var singleEcgDialogFragment: SingleEcgDialogFragment? = null
    private var leadsIndex = 0
    private var onTick: ((Int) -> Unit)? = null
    private var onStop: (() -> Unit)? = null
    private var onCompleted: (() -> Unit)? = null
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        arguments?.let {
            orderId = it.getLong(KEY_ORDER_ID)
            devices = it.getSerializable(KEY_DEVICES) as? List<Info>
            mBinding.tvName.text = it.getString(KEY_NAME, "")
            mBinding.tvAge.text = it.getString(KEY_AGE)
            mBinding.tvSex.text = it.getString(KEY_SEX, "")
            mBinding.tvHeight.text = it.getString(KEY_HEIGHT)
            mBinding.tvWeight.text = it.getString(KEY_WEIGHT)
        }
        if (orderId == 0L || devices.isNullOrEmpty()) {
            return null
        }
        mBinding.tvEcgParams.text = getEcgParams()
        mBinding.ecgView.apply {
            setGridSize(10f.dp)
            setBgPainter(createBgPainter())
        }
        mProgressDialog = ProgressDialog(requireContext(), "正在初始化，请稍后……")
        collectUiState()
        return mBinding.root
    }

    override fun onLazyLoadData() {
        lifecycleScope.launch {
            mProgressDialog.show()
            mViewModel.init(requireActivity(), orderId, devices)
            // 以下的代码都使用到了mViewModel，所以需要等待mViewModel初始化完成之后再执行
            mBinding.btnStart.setOnClickListener {
                if (mBinding.btnStart.text == "开始") {
                    mBinding.btnStart.text = "紧急停止"
                    mViewModel.startTimer()
                    mViewModel.connect()
                } else {
                    onStop?.invoke()
                }
            }
            devices?.forEach {
                when (it) {
                    is BleInfo -> {
                        when (it.deviceType) {
                            DeviceType.HeartRate -> {
                                mBinding.tvHeartRateName.text = "(${it.name})"
                            }

                            DeviceType.BloodOxygen -> {
                                mBinding.tvBloodOxygenName.text = "(${it.name})"
                            }

                            DeviceType.BloodPressure -> {
                                mBinding.tvBloodPressureName.text = "(${it.name})"
                                mBinding.btnBloodPressureBefore.setOnClickListener {
                                    mViewModel.measureBloodPressureBefore()
                                }
                                mBinding.btnBloodPressureAfter.setOnClickListener {
                                    mViewModel.measureBloodPressureAfter()
                                }
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
            initEcgView()
            mProgressDialog.dismiss()
        }
    }

    private fun initEcgView() {
        devices?.forEach {
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            val sampleRate = mViewModel.getSampleRate()
                            val leadsNames = listOf("I")
                            mBinding.ecgView.setDataPainters(listOf(createDynamicDataPainter())) {
                                leadsIndex = it
                                singleEcgDialogFragment = SingleEcgDialogFragment.newInstance(
                                    sampleRate, leadsNames[it], getEcgParams()
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

                is SocketInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            val sampleRate = mViewModel.getSampleRate()
                            val leadsNames = listOf(
                                "I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"
                            )
                            mBinding.ecgView.setDataPainters((0 until 12).map { createDynamicDataPainter() }) {
                                leadsIndex = it
                                singleEcgDialogFragment = SingleEcgDialogFragment.newInstance(
                                    sampleRate, leadsNames[it], getEcgParams()
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
            collectDistinctProperty(DevicesUiState::progress) {
                mBinding.pb.progress = it
                onTick?.invoke(it)
            }
            collectDistinctProperty(DevicesUiState::completed) {
                if (it) {
                    onCompleted?.invoke()
                }
            }
            collectDistinctProperty(DevicesUiState::healthInfo) {
                mBinding.tvBloodPressureBeforeSbp.text = it.bloodPressureBefore?.sbp?.toString() ?: "---"
                mBinding.tvBloodPressureBeforeDbp.text = it.bloodPressureBefore?.dbp?.toString() ?: "---"
                mBinding.tvBloodPressureAfterSbp.text = it.bloodPressureAfter?.sbp?.toString() ?: "---"
                mBinding.tvBloodPressureAfterDbp.text = it.bloodPressureAfter?.dbp?.toString() ?: "---"
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
            collectNotHandledEventProperty(DevicesUiState::toastEvent) {
                requireContext().showToast(toastEvent = it)
            }
        }
    }

    private fun getEcgParams() = "$mm_per_s mm/s    $mm_per_mv mm/mV"

    /**
     * 运动完成监听
     */
    fun setOnCompletedListener(onCompleted: () -> Unit) {
        this.onCompleted = onCompleted
    }

    /**
     * 紧急停止按钮点击监听
     */
    fun setOnStopListener(onStop: () -> Unit) {
        this.onStop = onStop
    }

    /**
     * 设置秒数回调监听
     */
    fun setOnTickListener(onTick: (Int) -> Unit) {
        this.onTick = onTick
    }

    /**
     * 设置记圈设备名称
     */
    fun setLapName(name: String) {
        mBinding.tvLapName.text = "($name)"
    }

    /**
     * 更新记圈设备连接状态
     * @param status 连接状态。包括："未连接"、"已连接"、"已断开"
     */
    fun updateLapStatus(status: String) {
        mBinding.tvLapStatus.text = status
    }

    /**
     * 更新记圈设备米数
     */
    fun updateLapMeters(meters: String) {
        mBinding.tvLapMeters.text = meters
    }

    /**
     * 更新记圈设备圈数
     */
    fun updateLapCount(count: String) {
        mBinding.tvLapCount.text = count
    }

    /**
     * 更新增益
     */
    fun updateMmPerMv(mm_per_mv: Int) {
        this.mm_per_mv = mm_per_mv
        mBinding.ecgView.setMmPerMv(mm_per_mv)
        mBinding.tvEcgParams.text = getEcgParams()
    }

    /**
     * 更新走纸速度
     */
    fun updateMmPerS(mm_per_s: Int) {
        this.mm_per_s = mm_per_s
        mBinding.ecgView.setMmPerS(mm_per_s)
        mBinding.tvEcgParams.text = getEcgParams()
    }

}
