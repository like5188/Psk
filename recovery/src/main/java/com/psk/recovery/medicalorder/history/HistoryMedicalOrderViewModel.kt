package com.psk.recovery.medicalorder.history

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.DeviceRepository
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.seeker.luckychart.model.ECGPointValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 医嘱回放规则：
 * 1、开始时间为医嘱实际执行时间
 * 2、结束时间为医嘱实际结束时间
 * 3、时间轴每秒变化一次
 */
@OptIn(KoinApiExtension::class)
class HistoryMedicalOrderViewModel(
    private val deviceRepository: DeviceRepository,
) : KoinComponent, ViewModel() {
    private val _uiState = MutableStateFlow(HistoryMedicalOrderUiState())
    val uiState = _uiState.asStateFlow()
    private lateinit var medicalOrder: MedicalOrder
    private lateinit var monitorDevices: List<MonitorDevice>

    /**
     * 是否开始，用于控制开始暂停
     */
    private val isStart = AtomicBoolean(false)

    /**
     * 更新uiState的时间间隙，用于控制快进
     */
    var updateUiStateInterval = 1000L
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd hh:mm:ss"))
    private var bloodOxygenList: List<BloodOxygen>? = null
    private var bloodPressureList: List<BloodPressure>? = null
    private var heartRateList: List<HeartRate>? = null
    private var allTimeLines: List<Long>? = null
    private var curProgress = 0
    private var getAllHistoryDataAndEmitJob: Job? = null

    init {
        getHistoryDataAndCache()
    }

    private fun getHistoryDataAndCache() {
        // 获取历史数据并缓存起来
        viewModelScope.launch(Dispatchers.IO) {
            monitorDevices.forEach {
                // 0：血氧仪；1：血压仪；2：心电；
                when (it.type) {
                    0 -> bloodOxygenList = deviceRepository.getBloodOxygenByMedicalOrderId(medicalOrder.id)
                    1 -> bloodPressureList = deviceRepository.getBloodPressureByMedicalOrderId(medicalOrder.id)
                    2 -> heartRateList = deviceRepository.getHeartRateByMedicalOrderId(medicalOrder.id)
                }
            }
            Log.i(TAG, "bloodOxygenList=$bloodOxygenList")
            Log.i(TAG, "bloodPressureList=$bloodPressureList")
            Log.i(TAG, "heartRateList=$heartRateList")
            if (bloodOxygenList.isNullOrEmpty() && bloodPressureList.isNullOrEmpty() && heartRateList.isNullOrEmpty()) {
                return@launch
            }
            // 获取时间轴（有数据的时间点的集合）
            val dataTimeLines = getDataTimeLines(bloodOxygenList, bloodPressureList, heartRateList)
            // 优化，让TextSeekBar时间轴能完整的从头到尾走完。
            val allTimeLines = dataTimeLines.toMutableList()
            val startTime = medicalOrder.startTime
            val endTime = if (medicalOrder.endTime == 0L) {
                // 如果没有结束时间（比如没有执行完，但是过期了）
                dataTimeLines.last()
            } else {
                medicalOrder.endTime
            }
            if (startTime != dataTimeLines.first()) {
                allTimeLines.add(0, startTime)
            }
            if (endTime != dataTimeLines.last()) {
                allTimeLines.add(endTime)
            }
            Log.i(TAG, "startTime=$startTime, endTime=$endTime, dataTimeLines=$dataTimeLines, allTimeLines=$allTimeLines")
            this@HistoryMedicalOrderViewModel.allTimeLines = allTimeLines
            _uiState.update { it.copy(maxProgress = allTimeLines.size - 1) }
        }
    }

    fun setMedicalOrderAndMonitorDevice(medicalOrderAndMonitorDevice: MedicalOrderAndMonitorDevice) {
        this.medicalOrder = medicalOrderAndMonitorDevice.medicalOrder
        this.monitorDevices = medicalOrderAndMonitorDevice.monitorDevices
    }

    fun start() {
        if (isStart.compareAndSet(false, true)) {
            getAllHistoryDataAndEmit()
        }
    }

    fun pause() {
        isStart.compareAndSet(true, false)
    }

    fun setMultiple(multiple: Int) {
        updateUiStateInterval = 1000L / multiple
    }

    /**
     * 从缓存中获取所有历史数据并按照一定的频率发送给UI显示
     */
    private fun getAllHistoryDataAndEmit() {
        if (getAllHistoryDataAndEmitJob != null) {
            // 避免频繁开始暂停造成调用多次
            return
        }
        val allTimeLines = this.allTimeLines
        if (allTimeLines.isNullOrEmpty()) {
            _uiState.update { it.copy(toastEvent = Event(ToastEvent(text = "获取数据失败，请稍后重试！"))) }
            return
        }
        getAllHistoryDataAndEmitJob = viewModelScope.launch(Dispatchers.IO) {
            // 每秒更新一次数据。分为是否有心率数据，因为需要保证心电图连续。
            (curProgress until allTimeLines.size).forEach {
                val second = allTimeLines[it]
                val list = getHistoryMedicalOrderUiStates(allTimeLines, second, true)
                Log.v(TAG, "second=$second time=${sdf.format(second * 1000)} listSize=${list.size}")
                if (list.size == 1) {
                    updateUiStateIfStart(list.first())
                    delay(updateUiStateInterval)
                } else if (list.size > 1) {
                    list.forEach {
                        updateUiStateIfStart(it)
                        delay(updateUiStateInterval / list.size)
                    }
                }
            }
        }
    }

    /**
     * 从缓存中获取指定进度（及指定时间点）的历史数据并发送给UI显示
     */
    fun getHistoryDataByProgress(progress: Int) {
        getAllHistoryDataAndEmitJob?.cancel()
        getAllHistoryDataAndEmitJob = null
        val allTimeLines = this.allTimeLines
        if (allTimeLines.isNullOrEmpty()) {
            _uiState.update { it.copy(toastEvent = Event(ToastEvent(text = "获取数据失败，请稍后重试！"))) }
            return
        }
        curProgress = progress
        val second = allTimeLines[progress]
        val list = getHistoryMedicalOrderUiStates(allTimeLines, second, false)
        Log.v(TAG, "progress=$progress second=$second time=${sdf.format(second * 1000)} listSize=${list.size}")
        // 只可能有 0 个或者 1 个数据，因为 getHistoryMedicalOrderUiStates 方法的参数 needAnim 为 false
        if (list.size == 1) {
            _uiState.update { list.first() }
        }
    }

    private fun getHistoryMedicalOrderUiStates(
        allTimeLines: List<Long>,
        second: Long,
        needAnim: Boolean
    ): List<HistoryMedicalOrderUiState> {
        val oldUiState = _uiState.value.copy(
            progress = allTimeLines.indexOf(second),
            curTimeString = sdf.format(second * 1000),
            bloodOxygen = bloodOxygenList?.lastOrNull {
                it.time == second
            },
            bloodPressure = bloodPressureList?.lastOrNull {
                it.time == second
            }
        )
        val heartRateList = heartRateList
        // 每秒更新一次数据。分为是否有心率数据，因为需要保证心电图连续。
        if (heartRateList.isNullOrEmpty()) {
            return listOf(oldUiState)
        } else {
            // 每秒钟的数据量
            val count = heartRateList.firstOrNull {
                it.values.isNotEmpty()
            }?.values?.size ?: 0
            // 指定second的心率数据
            val heartRate = heartRateList.lastOrNull {
                it.time == second
            }
            if (heartRate != null) {
                val heartRates = heartRate.values
                val coorYValues = heartRate.coorYValues
                return if (needAnim) {
                    // 如果需要动画，就把每一秒的数据分开成count个数据发给UI，这样才能形成1秒中的动画
                    (0 until count).map {
                        oldUiState.copy(
                            heartRate = heartRates[it],
                            ecgPointValue = createECGPointValue(coorYValues[it])
                        )
                    }
                } else {
                    // 如果不需要动画，就把每一秒的数据一起发给UI
                    listOf(
                        oldUiState.copy(
                            heartRate = heartRates.lastOrNull(),
                            ecgPointValues = coorYValues.map { createECGPointValue(it) }
                        )
                    )
                }
            } else if (second == allTimeLines.first() || second == allTimeLines.last()) {
                // 第一个和最后一个时间点的数据可以为null，
                // 因为第一个时间点肯定没有数据，因为心率数据会在第1秒开始获取，第2秒得到的数据就是第1秒的；
                // 最后一个时间点有可能是暂停后过段时间再结束的，也可能没有数据。
                return if (needAnim) {
                    (0 until count).map {
                        oldUiState.copy(
                            heartRate = 0,
                            ecgPointValue = createECGPointValue(0f)
                        )
                    }
                } else {
                    listOf(
                        oldUiState.copy(
                            heartRate = 0,
                            ecgPointValues = (0 until count).map { createECGPointValue(0f) }
                        )
                    )
                }
            } else {
                // 保证心电图连续不断。当中间时间点（除了第一个和最后一个时间点）没有心电数据时，不更新任何数据
                return emptyList()
            }
        }
    }

    /**
     * 支持暂停更新
     */
    private suspend fun updateUiStateIfStart(newUiState: HistoryMedicalOrderUiState) = withContext(Dispatchers.IO) {
        while (isActive) {
            if (isStart.get()) {
                _uiState.update { newUiState }
                break
            } else {
                delay(10)
            }
        }
    }

    /**
     * 获取有效的时间轴集合
     */
    private fun getDataTimeLines(
        bloodOxygenList: List<BloodOxygen>?, bloodPressureList: List<BloodPressure>?, heartRateList: List<HeartRate>?
    ): List<Long> {
        val bloodOxygenTimeLines = bloodOxygenList?.map {
            it.time
        } ?: emptyList()
        val bloodPressureTimeLines = bloodPressureList?.map {
            it.time
        } ?: emptyList()
        val heartRateTimeLines = heartRateList?.map {
            it.time
        } ?: emptyList()
        val timeLines = if (heartRateList.isNullOrEmpty()) {
            // 如果没有心率，当血压血氧其中之一有数据(肯定会有，因为时间轴是有数据的时间点的集合)，则更新，然后延迟1秒
            sortedSetOf(*(bloodOxygenTimeLines + bloodPressureTimeLines).toTypedArray())
        } else {
            // 如果有心率，则以心率为主，保证心电图连续不断。当某一秒没有心电数据时，不更新任何数据，则不加入时间轴。
            sortedSetOf(*heartRateTimeLines.toTypedArray())
        }
        return timeLines.toList()
    }

    private fun createECGPointValue(coorY: Float): ECGPointValue = ECGPointValue().apply {
        this.coorY = coorY
        this.drawColor = Color.parseColor("#00FF00")
        this.index = 0
        this.isNewStart = false
        this.coorX = 0f
        this.type = 2
    }

    companion object {
        private val TAG = HistoryMedicalOrderViewModel::class.java.simpleName
    }

}
