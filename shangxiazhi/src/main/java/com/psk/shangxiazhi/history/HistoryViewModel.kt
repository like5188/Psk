package com.psk.shangxiazhi.history

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
    private lateinit var dateList: List<String>

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
            if (bloodOxygenList.isNullOrEmpty() && bloodPressureList.isNullOrEmpty() && heartRateList.isNullOrEmpty() && shangXiaZhiList.isNullOrEmpty()) {
                return@launch
            }
            // 获取所有训练的开始时间的集合
            dateList = getDataTimeLines(bloodOxygenList, bloodPressureList, heartRateList, shangXiaZhiList)
            _uiState.update {
                it.copy(showTime = dateList.lastOrNull())
            }
        }
    }

    /**
     * 获取所有训练的开始时间的集合。
     */
    private fun getDataTimeLines(
        bloodOxygenList: List<BloodOxygen>?,
        bloodPressureList: List<BloodPressure>?,
        heartRateList: List<HeartRate>?,
        shangXiaZhiList: List<ShangXiaZhi>?
    ): List<String> {
        val bloodOxygenTimeLines = bloodOxygenList?.groupBy {
            it.medicalOrderId
        }?.map {
            sdf.format(it.value.first().time)
        } ?: emptyList()
        val bloodPressureTimeLines = bloodPressureList?.groupBy {
            it.medicalOrderId
        }?.map {
            sdf.format(it.value.first().time)
        } ?: emptyList()
        val heartRateTimeLines = heartRateList?.groupBy {
            it.medicalOrderId
        }?.map {
            sdf.format(it.value.first().time)
        } ?: emptyList()
        val shangXiaZhiTimeLines = shangXiaZhiList?.groupBy {
            it.medicalOrderId
        }?.map {
            sdf.format(it.value.first().time)
        } ?: emptyList()
        val timeLines = sortedSetOf(
            *(bloodOxygenTimeLines + bloodPressureTimeLines + heartRateTimeLines + shangXiaZhiTimeLines).toTypedArray()
        )
        return timeLines.toList()
    }

    fun getBloodOxygenList(medicalOrderId: Long): List<BloodOxygen> {
        return emptyList()
    }

    fun getBloodPressureList(medicalOrderId: Long): List<BloodPressure> {
        return emptyList()
    }

    fun getHeartRateList(medicalOrderId: Long): List<HeartRate> {
        return emptyList()
    }

    fun getShangXiaZhiList(medicalOrderId: Long): List<ShangXiaZhi> {
        return emptyList()
    }

    fun getPreTime() {
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dateList = this.dateList
        if (dateList.isEmpty()) {
            return
        }
        val index = dateList.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index - 1 >= 0) {
            _uiState.update {
                it.copy(showTime = dateList[index - 1])
            }
        }
    }

    fun getNextTime() {
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dateList = this.dateList
        if (dateList.isEmpty()) {
            return
        }
        val index = dateList.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index + 1 >= 0) {
            _uiState.update {
                it.copy(showTime = dateList[index + 1])
            }
        }
    }

    companion object {
        private val TAG = HistoryViewModel::class.java.simpleName
    }

}