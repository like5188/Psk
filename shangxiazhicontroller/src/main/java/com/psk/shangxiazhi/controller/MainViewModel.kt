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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val bleDeviceRepository: ShangXiaZhiRepository by lazy {
        RepositoryManager.createBleDeviceRepository(DeviceType.ShangXiaZhi)
    }
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
        bleDeviceRepository.connect(viewModelScope, {
            _uiState.update {
                it.copy(connectState = "已连接", isConnected = true)
            }
            fetch()
        }, {
            _uiState.update {
                it.copy(connectState = "未连接", isConnected = false)
            }
        })
    }

    private fun fetch() {
        viewModelScope.launch {
            bleDeviceRepository.fetch().collect { shangXiaZhi ->
                _uiState.update {
                    it.copy(shangXiaZhi = shangXiaZhi)
                }
            }
        }
    }

    private fun isRunning(): Boolean {
        return (_uiState.value.shangXiaZhi?.speed ?: 0) > 0
    }

    fun start(params: ShangXiaZhiParams) {
        if (isRunning()) {
            return
        }
        viewModelScope.launch {
            bleDeviceRepository.setParams(params)
            delay(100)
            bleDeviceRepository.start()
        }
    }

    fun pause() {
        viewModelScope.launch {
            bleDeviceRepository.pause()
        }
    }

    fun stop() {
        viewModelScope.launch {
            bleDeviceRepository.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleDeviceRepository.close()
    }

}
