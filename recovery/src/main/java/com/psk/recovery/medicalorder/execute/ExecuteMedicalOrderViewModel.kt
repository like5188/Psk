package com.psk.recovery.medicalorder.execute

import android.graphics.Color
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.common.util.asFlow
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.DeviceRepository
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.data.source.RecoveryRepository
import com.seeker.luckychart.model.ECGPointValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class ExecuteMedicalOrderViewModel(
    private val startOrPauseManager: StartOrPauseManager,
    private val deviceRepository: DeviceRepository,
    private val recoveryRepository: RecoveryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExecuteMedicalOrderUiState())
    val uiState = _uiState.asStateFlow()
    private var fetchBloodOxygenAndSaveJob: Job? = null
    private var fetchBloodPressureAndSaveJob: Job? = null
    private var fetchHeartRateAndSaveJob: Job? = null
    private lateinit var medicalOrder: MedicalOrder
    private lateinit var monitorDevices: List<MonitorDevice>

    fun setMedicalOrderAndMonitorDevice(medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice) {
        this.medicalOrder = medicalOrderAndMonitorDevice.medicalOrder
        this.monitorDevices = medicalOrderAndMonitorDevice.monitorDevices
        val remainTime = medicalOrder.remainInterval
        updateRemainTimeUI(remainTime)

        with(startOrPauseManager) {
            totalInterval = remainTime
            onStart = {
                Log.i(TAG, "开始获取数据并存储到数据库中")
                startFetchAndSaveJob()
                updateMedicalOrderStartTimeToDb()
                _uiState.update {
                    it.copy(startOrPause = "暂停", startOrPauseEnable = true)
                }
            }
            onResume = {
                Log.i(TAG, "恢复获取数据并存储到数据库中")
                startFetchAndSaveJob()
                _uiState.update {
                    it.copy(startOrPause = "暂停", startOrPauseEnable = true)
                }
            }
            onPause = { isPauseByDisconnected ->
                Log.w(TAG, "暂停获取数据并存储到数据库")
                cancelFetchAndSaveJob()
                _uiState.update {
                    it.copy(startOrPause = "开始", startOrPauseEnable = !isPauseByDisconnected)
                }
            }
            onTick = {
                updateRemainTimeUI(it)
                updateMedicalOrderRemainIntervalToDb(it)
            }
            onFinish = {
                Log.w(TAG, "结束获取数据并存储到数据库")
                cancelFetchAndSaveJob()
                updateMedicalOrderEndTimeToDb()
                _uiState.update {
                    it.copy(startOrPause = "完成", startOrPauseEnable = false)
                }
            }
        }

        // 监听数据库中最新的数据并连接设备
        val curTime = System.currentTimeMillis() / 1000
        monitorDevices.forEach {
            // 0：血氧仪；1：血压仪；2：心电；
            when (it.type) {
                0 -> {
                    getBloodOxygen(deviceRepository.listenLatestBloodOxygen(curTime))
                    deviceRepository.enableBloodOxygen()
                }

                1 -> {
                    getBloodPressure(deviceRepository.listenLatestBloodPressure(curTime))
                    deviceRepository.enableBloodPressure()
                }

                2 -> {
                    getHeartRate(deviceRepository.listenLatestHeartRate(curTime))
                    deviceRepository.enableHeartRate()
                }
            }
        }
        deviceRepository.connectAll(onConnected = {
            startOrPauseManager.connectOne()
        }) {
            startOrPauseManager.disconnectOne()
        }
    }

    private fun updateRemainTimeUI(remainTime: Long) {
        val min = remainTime / 60
        val sec = remainTime % 60
        val minStr = if (min < 10) "0$min" else "$min"
        val secStr = if (sec < 10) "0$sec" else "$sec"
        _uiState.update {
            it.copy(time = "$minStr:$secStr")
        }
    }

    /**
     * 更新医嘱剩余时长和状态
     */
    private fun updateMedicalOrderRemainIntervalToDb(remainInterval: Long) {
        medicalOrder = medicalOrder.copy(remainInterval = remainInterval)
        viewModelScope.launch {
            recoveryRepository.updateMedicalOrders(medicalOrder)
        }
    }

    /**
     * 更新医嘱开始时间和状态
     */
    private fun updateMedicalOrderStartTimeToDb() {
        medicalOrder = medicalOrder.copy(
            startTime = System.currentTimeMillis() / 1000, status = 1
        )
        viewModelScope.launch {
            recoveryRepository.updateMedicalOrders(medicalOrder)
        }
    }

    /**
     * 更新医嘱结束时间和状态
     */
    private fun updateMedicalOrderEndTimeToDb() {
        medicalOrder = medicalOrder.copy(
            endTime = System.currentTimeMillis() / 1000, status = 2
        )
        viewModelScope.launch {
            recoveryRepository.updateMedicalOrders(medicalOrder)
        }
    }

    private fun startFetchAndSaveJob() {
        monitorDevices.forEach {
            // 0：血氧仪；1：血压仪；2：心电；
            when (it.type) {
                0 -> {
                    fetchBloodOxygenAndSave()
                }

                1 -> {
                    fetchBloodPressureAndSave()
                }

                2 -> {
                    fetchHeartRateAndSave()
                }
            }
        }
    }

    private fun cancelFetchAndSaveJob() {
        monitorDevices.forEach {
            // 0：血氧仪；1：血压仪；2：心电；
            when (it.type) {
                0 -> {
                    fetchBloodOxygenAndSaveJob?.cancel()
                    fetchBloodOxygenAndSaveJob = null
                }

                1 -> {
                    fetchBloodPressureAndSaveJob?.cancel()
                    fetchBloodPressureAndSaveJob = null
                }

                2 -> {
                    fetchHeartRateAndSaveJob?.cancel()
                    fetchHeartRateAndSaveJob = null
                }
            }
        }
    }

    /**
     * 结束"从蓝牙设备获取数据并保存到数据库中"
     */
    fun finish() {
        cancelFetchAndSaveJob()
        updateMedicalOrderEndTimeToDb()
    }

    /**
     * 开始或者暂停"从蓝牙设备获取数据并保存到数据库中"
     */
    @MainThread
    fun startOrPause() {
        startOrPauseManager.startOrPause()
    }

    private fun getBloodOxygen(flow: Flow<BloodOxygen?>) {
        viewModelScope.launch {
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getBloodOxygen value=$value")
                _uiState.update {
                    it.copy(bloodOxygen = value)
                }
            }
        }
    }

    private fun getBloodPressure(flow: Flow<BloodPressure?>) {
        viewModelScope.launch {
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getBloodPressure value=$value")
                _uiState.update {
                    it.copy(bloodPressure = value)
                }
            }
        }
    }

    private fun getHeartRate(flow: Flow<HeartRate?>) {
        viewModelScope.launch {
            flow.filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                Log.v(TAG, "getHeartRate value=$value")
                _uiState.update {
                    it.copy(heartRate = value)
                }
            }
        }
        viewModelScope.launch {
            var count = 0
            flow.filterNotNull().flatMapConcat {
                count = it.coorYValues.size
                it.coorYValues.asFlow()
            }.map {
                ECGPointValue().apply {
                    this.coorY = it
                    this.drawColor = Color.parseColor("#00FF00")
                    this.index = 0
                    this.isNewStart = false
                    this.coorX = 0f
                    this.type = 2
                }
            }.buffer(count).onEach {
                // count 为心电数据采样率，即 1 秒钟采集 count 次数据。
                delay(1000L / count)
            }.collect { value ->
                Log.v(TAG, "getECGPointValue value=$value")
                _uiState.update {
                    it.copy(ecgPointValue = value)
                }
            }
        }
    }

    private fun fetchBloodOxygenAndSave() {
        if (fetchBloodOxygenAndSaveJob != null) {
            return
        }
        fetchBloodOxygenAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d(TAG, "fetchBloodOxygenAndSave")
                deviceRepository.fetchBloodOxygenAndSave(medicalOrder.id)
                delay(1000)
            }
        }
    }

    private fun fetchBloodPressureAndSave() {
        if (fetchBloodPressureAndSaveJob != null) {
            return
        }
        fetchBloodPressureAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d(TAG, "fetchBloodPressureAndSave")
                deviceRepository.fetchBloodPressureAndSave(medicalOrder.id)
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(1000)
            }
        }
    }

    private fun fetchHeartRateAndSave() {
        if (fetchHeartRateAndSaveJob != null) {
            return
        }
        fetchHeartRateAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchHeartRateAndSave")
            try {
                deviceRepository.fetchHeartRateAndSave(medicalOrder.id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(toastEvent = Event(ToastEvent(throwable = e)))
                }
            }
        }
    }

    companion object {
        private val TAG = ExecuteMedicalOrderViewModel::class.java.simpleName
    }

}
