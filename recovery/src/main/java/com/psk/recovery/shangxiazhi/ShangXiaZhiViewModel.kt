package com.psk.recovery.shangxiazhi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.data.source.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ShangXiaZhiViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private var fetchShangXiaZhiAndSaveJob: Job? = null

    fun start(
        passiveModule: Boolean = true,
        timeInt: Int = 5,
        speedInt: Int = 20,
        spasmInt: Int = 3,
        resistanceInt: Int = 1,
        intelligent: Boolean = true,
        turn2: Boolean = true
    ) {
        getShangXiaZhi(deviceRepository.listenLatestShangXiaZhi(0))
        deviceRepository.connectShangXiaZhi(onConnected = {
            Log.w(TAG, "上下肢连接成功")
            viewModelScope.launch {
                Log.w(
                    TAG,
                    "设置参数：passiveModule=$passiveModule timeInt=$timeInt speedInt=$speedInt spasmInt=$spasmInt resistanceInt=$resistanceInt intelligent=$intelligent turn2=$turn2"
                )
                deviceRepository.setShangXiaZhiParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
                delay(100)
                deviceRepository.startShangXiaZhi()
                delay(100)
                fetchShangXiaZhiAndSave()
            }
        }) {
            Log.e(TAG, "上下肢连接失败")
        }
    }

    fun stopShangXiaZhi() {
        viewModelScope.launch {
            deviceRepository.stopShangXiaZhi()
        }
    }

    fun pauseShangXiaZhi() {
        viewModelScope.launch {
            deviceRepository.pauseShangXiaZhi()
        }
    }

    fun finish() {
        cancelFetchAndSaveJob()
    }

    private fun getShangXiaZhi(flow: Flow<ShangXiaZhi?>) {
        viewModelScope.launch {
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getShangXiaZhi value=$value")
            }
        }
    }

    private fun fetchShangXiaZhiAndSave() {
        fetchShangXiaZhiAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchShangXiaZhiAndSave")
            try {
                deviceRepository.fetchShangXiaZhiAndSave(1)
            } catch (e: Exception) {
            }
        }
    }

    private fun cancelFetchAndSaveJob() {
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
    }

    companion object {
        private val TAG = ShangXiaZhiViewModel::class.java.simpleName
    }

}
