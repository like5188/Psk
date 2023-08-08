package com.psk.recovery.shangxiazhi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.data.source.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ShangXiaZhiViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private var fetchShangXiaZhiAndSaveJob: Job? = null

    fun start() {
        startFetchAndSaveJob()
        getShangXiaZhi(deviceRepository.listenLatestShangXiaZhi(0))
        connectShangXiaZhi()
    }

    private fun startFetchAndSaveJob() {
        fetchHeartRateAndSave()
    }

    private fun cancelFetchAndSaveJob() {
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
    }

    /**
     * 结束"从蓝牙设备获取数据并保存到数据库中"
     */
    fun finish() {
        cancelFetchAndSaveJob()
    }

    private fun connectShangXiaZhi() {
        deviceRepository.connectShangXiaZhi(onConnected = {
            println("上下肢连接成功")
        }) {
            println("上下肢连接失败")
        }
    }

    private fun getShangXiaZhi(flow: Flow<ShangXiaZhi?>) {
        viewModelScope.launch {
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getShangXiaZhi value=$value")
            }
        }
    }

    private fun fetchHeartRateAndSave() {
        fetchShangXiaZhiAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchHeartRateAndSave")
            try {
                deviceRepository.fetchShangXiaZhiAndSave(1)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val TAG = ShangXiaZhiViewModel::class.java.simpleName
    }

}
