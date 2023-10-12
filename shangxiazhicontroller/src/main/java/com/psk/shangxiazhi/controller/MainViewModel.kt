package com.psk.shangxiazhi.controller

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.ble.util.PermissionUtils
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val bleDeviceRepository: ShangXiaZhiRepository by lazy {
        RepositoryManager.createBleDeviceRepository(DeviceType.ShangXiaZhi)
    }
    private var shangXiaZhiParams = ShangXiaZhiParams(true, 5, 4, 6, 0, true, true)

    fun init(activity: ComponentActivity) {
        viewModelScope.launch {
            PermissionUtils.requestScanEnvironment(activity)
            PermissionUtils.requestConnectEnvironment(activity)
            RepositoryManager.init(activity.applicationContext)
        }
    }

    fun connect(context: Context, name: String, address: String, onConnected: () -> Unit, onDisconnected: () -> Unit) {
        bleDeviceRepository.enable(context, name, address)
        bleDeviceRepository.connect(viewModelScope, onConnected, onDisconnected)
    }

    fun fetch(): Flow<ShangXiaZhi> = bleDeviceRepository.fetch().onEach {
        shangXiaZhiParams = shangXiaZhiParams.copy(
            passiveModel = it.model.toInt() == 0x01,
            time = 0,
            speedLevel = it.speedLevel,
            spasmLevel = it.spasmLevel,
            resistance = it.resistance,
            intelligent = it.intelligence.toInt() == 0x41,
            forward = it.direction.toInt() == 0x51
        )
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
