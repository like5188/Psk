package com.psk.shangxiazhi.controller

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.DeviceRepositoryManager
import com.psk.device.ScanManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val bleDeviceRepository: ShangXiaZhiRepository by lazy {
        DeviceRepositoryManager.createBleDeviceRepository(DeviceType.ShangXiaZhi)
    }
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    private var isRunningInPassiveMode = false// 被动模式时，上下肢是否运行
    private var isRunningInActiveMode = false// 主动模式时，上下肢是否运行

    fun init(activity: ComponentActivity) {
        viewModelScope.launch {
            ScanManager.init(activity)
            DeviceRepositoryManager.init(activity)
        }
    }

    fun scan(activity: FragmentActivity) {
        ScanDeviceDialogFragment.newInstance(DeviceType.ShangXiaZhi).apply {
            onSelected = { bleScanInfo ->
                _uiState.update {
                    it.copy(name = bleScanInfo.name)
                }
                connect(activity, bleScanInfo.name, bleScanInfo.address)
            }
        }.show(activity)
    }

    private fun connect(context: Context, name: String, address: String) {
        _uiState.update {
            it.copy(connectState = "连接中……", isConnected = false)
        }
        bleDeviceRepository.init(context, name, address)
        bleDeviceRepository.setCallback(onStart = {}, onPause = {
            // 只有被动模式能暂停
            isRunningInPassiveMode = false
            _uiState.update {
                it.copy(isRunning = false)
            }
        }, onOver = {
            isRunningInPassiveMode = false
            isRunningInActiveMode = false
            _uiState.update {
                it.copy(isRunning = false)
            }
        })
        bleDeviceRepository.connect(viewModelScope, onConnected = {
            _uiState.update {
                it.copy(connectState = "已连接", isConnected = true)
            }
            fetch()
        }, onDisconnected = {
            _uiState.update {
                it.copy(connectState = "未连接", isConnected = false)
            }
        })
    }

    private fun fetch() {
        viewModelScope.launch {
            bleDeviceRepository.fetch().collect { shangXiaZhi ->
                val passiveModel = shangXiaZhi.model.toInt() == 0x01
                isRunningInPassiveMode = passiveModel && shangXiaZhi.speed > 0
                isRunningInActiveMode = !passiveModel && shangXiaZhi.speed > 0
                _uiState.update {
                    it.copy(shangXiaZhi = shangXiaZhi, isRunning = isRunningInPassiveMode || isRunningInActiveMode)
                }
            }
        }
    }

    fun start(params: ShangXiaZhiParams) {
        if (isRunningInActiveMode || isRunningInPassiveMode) {
            return
        }
        viewModelScope.launch {
            if (!params.passiveModel) {
                // 主动模式
                println("主动模式：启动")
                bleDeviceRepository.setParams(params)
            } else {
                // 被动模式
                println("被动模式：启动")
                if (bleDeviceRepository.setParams(params)) {
                    delay(100)
                    bleDeviceRepository.start()
                }
            }
        }
    }

    fun pause() {
        viewModelScope.launch {
            println("暂停")
            !bleDeviceRepository.pause()
        }
    }

    fun stop() {
        viewModelScope.launch {
            println("停止")
            !bleDeviceRepository.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleDeviceRepository.close()
    }

}
