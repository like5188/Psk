package com.psk.shangxiazhi.history

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
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Date

@OptIn(KoinApiExtension::class)
class HistoryViewModel : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private val deviceManager by inject<DeviceManager>()
    private val bloodOxygenRepository = deviceManager.createRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository = deviceManager.createRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = deviceManager.createRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val shangXiaZhiRepository = deviceManager.createRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi)
    private val unionRepository = deviceManager.unionRepository
    private var bloodOxygenList: List<BloodOxygen>? = null
    private var bloodPressureList: List<BloodPressure>? = null
    private var heartRateList: List<HeartRate>? = null
    private var shangXiaZhiList: List<ShangXiaZhi>? = null
    private var getAllHistoryDataAndEmitJob: Job? = null
    private lateinit var datas: Map<String, List<DateAndData>>

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val medicalOrderTimeMap = unionRepository.getAllMedicalOrderWithTime()
            if (medicalOrderTimeMap.isNullOrEmpty()) {
                return@launch
            }
            val cal = Calendar.getInstance()
            datas = medicalOrderTimeMap.map {
                cal.time = Date(it.value * 1000L)
                DateAndData(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH) + 1,
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    hour = cal.get(Calendar.HOUR),
                    minute = cal.get(Calendar.MINUTE),
                    second = cal.get(Calendar.SECOND),
                    data = it.key
                )
            }.groupBy {
                "${it.year}年${it.month.format2()}月"
            }
            _uiState.update {
                val key = datas.keys.lastOrNull()
                val value = datas[key]
                it.copy(
                    showTime = key,
                    dateAndDataList = value
                )
            }
        }
    }

    fun getPreTime() {
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = this.datas.keys.toList()
        if (dates.isEmpty()) {
            return
        }
        val index = dates.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index - 1 >= 0) {
            _uiState.update {
                val key = dates[index - 1]
                val value = this.datas[key]
                it.copy(
                    showTime = key,
                    dateAndDataList = value
                )
            }
        }
    }

    fun getNextTime() {
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = this.datas.keys.toList()
        if (dates.isEmpty()) {
            return
        }
        val index = dates.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index + 1 < dates.size) {
            _uiState.update {
                val key = dates[index + 1]
                val value = this.datas[key]
                it.copy(
                    showTime = key,
                    dateAndDataList = value
                )
            }
        }
    }

    fun getReports(medicalOrderId: Long): List<IReport> {
        return emptyList()
    }

    companion object {
        private val TAG = HistoryViewModel::class.java.simpleName
    }

}