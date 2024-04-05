package com.psk.sixminutes

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.SecondsTimer
import com.psk.common.util.scheduleFlow
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.business.MultiBusinessManager
import com.psk.sixminutes.model.BleInfo
import com.psk.sixminutes.model.Info
import com.psk.sixminutes.model.SocketInfo
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
    private val mSecondsTimer by lazy {
        SecondsTimer().apply {
            onTick = { seconds ->
                if (seconds > 8 * 60L) {
                    stop()
                    _uiState.update {
                        it.copy(
                            finish = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            time = "${decimalFormat.format(seconds / 60)}:${decimalFormat.format(seconds % 60)}"
                        )
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            scheduleFlow(0, 1000).collect {
                _uiState.update {
                    it.copy(
                        date = sdf.format(Date())
                    )
                }
            }
        }
    }

    suspend fun init(activity: ComponentActivity, devices: List<Info>?) {
        multiBusinessManager.init(activity, devices)
    }

    fun startTimer() {
        mSecondsTimer.start()
    }

    fun getSampleRate(info: Info) = if (info is BleInfo && info.deviceType == DeviceType.HeartRate) {
        multiBusinessManager.bleHeartRateBusinessManager.getSampleRate()
    } else if (info is SocketInfo && info.deviceType == DeviceType.HeartRate) {
        multiBusinessManager.socketHeartRateBusinessManager.getSampleRate()
    } else {
        0
    }

    fun connect(id: Long, devices: List<Info>?) {
        devices?.forEach {
            when (it) {
                is BleInfo -> {
                    when (it.deviceType) {
                        DeviceType.HeartRate -> {
                            multiBusinessManager.bleHeartRateBusinessManager.connect(
                                id,
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
                                id,
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
                                id,
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
        multiBusinessManager.bleBloodPressureBusinessManager.measure { sbp, dbp ->
            _uiState.update {
                it.copy(
                    sbpBefore = sbp.toString(),
                    dbpBefore = dbp.toString()
                )
            }
        }
    }

    fun measureBloodPressureAfter() {
        multiBusinessManager.bleBloodPressureBusinessManager.measure { sbp, dbp ->
            _uiState.update {
                it.copy(
                    sbpAfter = sbp.toString(),
                    dbpAfter = dbp.toString()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        multiBusinessManager.destroy()
        mSecondsTimer.stop()
    }

}
