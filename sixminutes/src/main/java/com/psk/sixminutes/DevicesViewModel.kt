package com.psk.sixminutes

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.common.util.scheduleFlow
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.business.MultiBusinessManager
import com.psk.sixminutes.data.db.SixMinutesDatabaseManager
import com.psk.sixminutes.data.model.BleInfo
import com.psk.sixminutes.data.model.Info
import com.psk.sixminutes.data.model.SocketInfo
import com.psk.sixminutes.data.source.HealthInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class DevicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState = _uiState.asStateFlow()
    private val sdf: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE")
    }
    private val decimalFormat = DecimalFormat("00")
    private val multiBusinessManager by lazy {
        MultiBusinessManager()
    }
    private val healthInfoRepository: HealthInfoRepository by lazy {
        HealthInfoRepository()
    }
    private var startTimer = false
    private var seconds = 0
    private var devices: List<Info>? = null

    init {
        viewModelScope.launch {
            scheduleFlow(0, 1000).collect {
                if (startTimer) {
                    seconds++
                    if (seconds <= 60 * 8) {// 8分钟
                        _uiState.update {
                            it.copy(
                                time = "${decimalFormat.format(seconds / 60)}:${decimalFormat.format(seconds % 60)}",
                                progress = seconds
                            )
                        }
                    } else {
                        if (!_uiState.value.completed && _uiState.value.healthInfo.bloodPressureAfter != null) {
                            disconnect()
                            _uiState.update {
                                it.copy(
                                    completed = true
                                )
                            }
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        date = sdf.format(Date())
                    )
                }
            }
        }
    }

    suspend fun init(activity: ComponentActivity, orderId: Long, devices: List<Info>?) {
        multiBusinessManager.init(activity, devices)
        SixMinutesDatabaseManager.init(activity.applicationContext)
        _uiState.update {
            it.copy(
                healthInfo = it.healthInfo.copy(
                    orderId = orderId
                )
            )
        }
        this.devices = devices
    }

    fun startTimer() {
        startTimer = true
    }

    fun getSampleRate(): Int {
        devices?.forEach {
            if (it is BleInfo && it.deviceType == DeviceType.HeartRate) {
                return multiBusinessManager.bleHeartRateBusinessManager.getSampleRate()
            } else if (it is SocketInfo && it.deviceType == DeviceType.HeartRate) {
                return multiBusinessManager.socketHeartRateBusinessManager.getSampleRate()
            }
        }
        return 0
    }

    fun disconnect() {
        multiBusinessManager.destroy()
    }

    fun connect() {
        val orderId = _uiState.value.healthInfo.orderId
        devices?.forEach {
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            multiBusinessManager.bleHeartRateBusinessManager.connect(
                                orderId,
                                onStatus = { status ->
                                    _uiState.update {
                                        it.copy(
                                            heartRateStatus = status
                                        )
                                    }
                                },
                                onHeartRateResult = { heartRate ->
                                    _uiState.update {
                                        it.copy(
                                            heartRate = heartRate.toString()
                                        )
                                    }
                                }) { ecgDatas ->
                                _uiState.update {
                                    it.copy(
                                        ecgDatas = ecgDatas
                                    )
                                }
                            }
                        }

                        DeviceType.BloodOxygen -> {
                            multiBusinessManager.bleBloodOxygenBusinessManager.connect(
                                orderId,
                                onStatus = { status ->
                                    _uiState.update {
                                        it.copy(
                                            bloodOxygenStatus = status
                                        )
                                    }
                                }
                            ) { bloodOxygen ->
                                _uiState.update {
                                    it.copy(
                                        bloodOxygen = bloodOxygen.toString()
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                }

                is SocketInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            multiBusinessManager.socketHeartRateBusinessManager.start(
                                orderId,
                                onStatus = { status ->
                                    _uiState.update {
                                        it.copy(
                                            heartRateStatus = status
                                        )
                                    }
                                },
                                onHeartRateResult = { heartRate ->
                                    _uiState.update {
                                        it.copy(
                                            heartRate = heartRate.toString()
                                        )
                                    }
                                }) { ecgDatas ->
                                _uiState.update {
                                    it.copy(
                                        ecgDatas = ecgDatas
                                    )
                                }
                            }
                        }

                        else -> {}
                    }

                }
            }
        }
    }

    fun measureBloodPressureBefore() {
        multiBusinessManager.bleBloodPressureBusinessManager.measure { bloodPressure ->
            val healthInfo = _uiState.value.healthInfo.copy(
                bloodPressureBefore = bloodPressure
            )
            viewModelScope.launch(Dispatchers.IO) {
                healthInfoRepository.insertOrUpdate(healthInfo)
                _uiState.update {
                    it.copy(
                        healthInfo = healthInfo,
                    )
                }
            }
        }
    }

    fun measureBloodPressureAfter() {
        multiBusinessManager.bleBloodPressureBusinessManager.measure { bloodPressure ->
            val healthInfo = _uiState.value.healthInfo.copy(
                bloodPressureAfter = bloodPressure
            )
            viewModelScope.launch(Dispatchers.IO) {
                healthInfoRepository.insertOrUpdate(healthInfo)
                _uiState.update {
                    it.copy(
                        healthInfo = healthInfo,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

}
