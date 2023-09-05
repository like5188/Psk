package com.psk.shangxiazhi.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.device.data.source.BloodPressureRepository
import com.psk.device.data.source.HeartRateRepository
import com.psk.device.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat

@OptIn(KoinApiExtension::class)
class HistoryViewModel : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private val sdf: SimpleDateFormat by inject(named("yyyy年MM月"))
    private val deviceManager by inject<DeviceManager>()
    private val bloodOxygenRepository = deviceManager.createRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository = deviceManager.createRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = deviceManager.createRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val shangXiaZhiRepository = deviceManager.createRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi)
    private var bloodOxygenList: List<BloodOxygen>? = null
    private var bloodPressureList: List<BloodPressure>? = null
    private var heartRateList: List<HeartRate>? = null
    private var shangXiaZhiList: List<ShangXiaZhi>? = null
    private var getAllHistoryDataAndEmitJob: Job? = null

    init {
        getHistoryDataAndCache()
    }

    private fun getHistoryDataAndCache() {
        // 获取历史数据并缓存起来
        viewModelScope.launch(Dispatchers.IO) {
            bloodOxygenList = bloodOxygenRepository.getAll()
            bloodPressureList = bloodPressureRepository.getAll()
            heartRateList = heartRateRepository.getAll()
            shangXiaZhiList = shangXiaZhiRepository.getAll()
            Log.i(TAG, "bloodOxygenList=$bloodOxygenList")
            Log.i(TAG, "bloodPressureList=$bloodPressureList")
            Log.i(TAG, "heartRateList=$heartRateList")
            Log.i(TAG, "shangXiaZhiList=$shangXiaZhiList")
        }
    }

    fun getDateList() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    dateList = listOf(
                        "2019年05月",
                        "2020年01月",
                        "2020年03月",
                        "2020年04月",
                        "2020年08月",
                        "2020年11月",
                        "2021年03月",
                    )
                )
            }
        }
    }

    fun getShangXiaZhiListByMedicalOrderId(context: Context): List<ShangXiaZhi> {
        return emptyList()
    }

    companion object {
        private val TAG = HistoryViewModel::class.java.simpleName
    }

}