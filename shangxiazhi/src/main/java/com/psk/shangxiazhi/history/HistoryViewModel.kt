package com.psk.shangxiazhi.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HealthInfo
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.device.data.source.BloodPressureRepository
import com.psk.device.data.source.HeartRateRepository
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.BloodPressureReport
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private val bloodOxygenRepository = RepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository = RepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = RepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val shangXiaZhiRepository = RepositoryManager.createBleDeviceRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi)
    private val unionRepository = RepositoryManager.unionRepository
    private val healthInfoRepository = RepositoryManager.healthInfoRepository
    private lateinit var datas: Map<String, List<DateAndData>>
    private val decimalFormat = DecimalFormat("00")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val medicalOrderTimeMap = unionRepository.getAllMedicalOrderWithTime()
            if (medicalOrderTimeMap.isNullOrEmpty()) {
                return@launch
            }
            val cal = Calendar.getInstance()
            datas = medicalOrderTimeMap.map {
                cal.time = Date(it.value)
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
                "${it.year}年${decimalFormat.format(it.month)}月"
            }
            _uiState.update {
                val key = datas.keys.lastOrNull()
                val value = datas[key]
                it.copy(
                    showTime = key, dateAndDataList = value
                )
            }
        }
    }

    fun getPreTime() {
        if (!::datas.isInitialized) {
            return
        }
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = datas.keys.toList()
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
                val value = datas[key]
                it.copy(
                    showTime = key, dateAndDataList = value
                )
            }
        }
    }

    fun getNextTime() {
        if (!::datas.isInitialized) {
            return
        }
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = datas.keys.toList()
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
                val value = datas[key]
                it.copy(
                    showTime = key, dateAndDataList = value
                )
            }
        }
    }

    suspend fun getReports(medicalOrderId: Long): List<IReport> {
        val result = mutableListOf<IReport>()
        bloodOxygenRepository.getListByMedicalOrderId(medicalOrderId)?.let {
            BloodOxygenReport.createForm(it.asFlow())
            result.add(BloodOxygenReport.report)
        }
        bloodPressureRepository.getListByMedicalOrderId(medicalOrderId)?.let {
            BloodPressureReport.createForm(it.asFlow())
            result.add(BloodPressureReport.report)
        }
        heartRateRepository.getListByMedicalOrderId(medicalOrderId)?.let {
            HeartRateReport.createForm(it.asFlow())
            result.add(HeartRateReport.report)
        }
        shangXiaZhiRepository.getListByMedicalOrderId(medicalOrderId)?.let {
            ShangXiaZhiReport.createForm(it.asFlow()).collect()
            result.add(ShangXiaZhiReport.report)
        }
        return result
    }

    suspend fun getHealthInfo(medicalOrderId: Long): HealthInfo? {
        return healthInfoRepository.getByMedicalOrderId(medicalOrderId)
    }

}