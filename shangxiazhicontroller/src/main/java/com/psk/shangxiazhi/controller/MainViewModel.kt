package com.psk.shangxiazhi.controller

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.ble.util.PermissionUtils
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val bleDeviceRepository: ShangXiaZhiRepository by lazy {
        RepositoryManager.createBleDeviceRepository(DeviceType.ShangXiaZhi)
    }
    private var shangXiaZhiParams = ShangXiaZhiParams(true, 5, 4, 6, 0, true, true)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun init(activity: ComponentActivity) {
        viewModelScope.launch {
            PermissionUtils.requestScanEnvironment(activity)
            PermissionUtils.requestConnectEnvironment(activity)
            RepositoryManager.init(activity.applicationContext)
        }
    }

    fun scan(activity: FragmentActivity) {
        ScanDeviceDialogFragment.newInstance(DeviceType.ShangXiaZhi).apply {
            onSelected = {
                _uiState.update {
                    it.copy(name = it.name)
                }
                _uiState.update {
                    it.copy(connectState = "连接中……")
                }
                connect(activity, it.name, it.address, {
                    _uiState.update {
                        it.copy(connectState = "已连接")
                    }
                    fetch()
                }) {
                    _uiState.update {
                        it.copy(connectState = "未连接")
                    }
                }
            }
        }.show(activity)
    }

    fun connect(context: Context, name: String, address: String, onConnected: () -> Unit, onDisconnected: () -> Unit) {
        bleDeviceRepository.init(context, name, address)
        bleDeviceRepository.connect(viewModelScope, onConnected, onDisconnected)
    }

    fun fetch() {
        viewModelScope.launch {
            bleDeviceRepository.fetch().onEach {
                shangXiaZhiParams = shangXiaZhiParams.copy(
                    passiveModel = it.model.toInt() == 0x01,
                    speedLevel = it.speedLevel,
                    spasmLevel = it.spasmLevel,
                    resistance = it.resistance,
                    intelligent = it.intelligence.toInt() == 0x41,
                    forward = it.direction.toInt() == 0x51
                )
            }.collect { shangXiaZhi ->
                _uiState.update {
                    it.copy(shangXiaZhi = shangXiaZhi)
                }
            }
        }
    }

    fun resume() {
        viewModelScope.launch {
            bleDeviceRepository.resume()
        }
    }

    fun pause() {
        viewModelScope.launch {
            bleDeviceRepository.pause()
        }
    }

    fun over() {
        viewModelScope.launch {
            bleDeviceRepository.over()
        }
    }

    fun setIntelligence() {
        setParams(shangXiaZhiParams.copy(intelligent = !shangXiaZhiParams.intelligent))
    }

    fun setForward() {
        setParams(shangXiaZhiParams.copy(forward = !shangXiaZhiParams.forward))
    }

    fun setSpeedLevel() {
        var speedLevel = shangXiaZhiParams.speedLevel
        if (speedLevel == 12) {
            speedLevel = 1
        } else {
            speedLevel++
        }
        setParams(shangXiaZhiParams.copy(speedLevel = speedLevel))
    }

    fun setResistance() {
        var resistance = shangXiaZhiParams.resistance
        if (resistance == 12) {
            resistance = 1
        } else {
            resistance++
        }
        setParams(shangXiaZhiParams.copy(resistance = resistance))
    }

    fun setPassiveModel() {
        setParams(shangXiaZhiParams.copy(passiveModel = !shangXiaZhiParams.passiveModel))
    }

    fun setSpasmLevel() {
        var spasmLevel = shangXiaZhiParams.spasmLevel
        if (spasmLevel == 12) {
            spasmLevel = 1
        } else {
            spasmLevel++
        }
        setParams(shangXiaZhiParams.copy(spasmLevel = spasmLevel))
    }

    fun setTime() {
        var time = shangXiaZhiParams.time
        if (time == 30) {
            time = 5
        } else {
            time += 5
        }
        shangXiaZhiParams = shangXiaZhiParams.copy(time = time)
        setParams(shangXiaZhiParams)
    }

    private fun setParams(params: ShangXiaZhiParams) {
        viewModelScope.launch {
            println(params)
            bleDeviceRepository.setParams(params)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleDeviceRepository.close()
    }

}
