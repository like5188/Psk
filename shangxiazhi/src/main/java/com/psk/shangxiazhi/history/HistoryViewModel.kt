package com.psk.shangxiazhi.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.min

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
        bloodOxygenRepository.getListByMedicalOrderId(medicalOrderId)?.lastOrNull()?.let {
            result.add(BloodOxygenReport().apply {
                value = it.value
            })
        }
        bloodPressureRepository.getListByMedicalOrderId(medicalOrderId)?.lastOrNull()?.let {
            result.add(BloodPressureReport().apply {
                sbp = it.sbp
                dbp = it.dbp
            })
        }
        val heartRateList = heartRateRepository.getListByMedicalOrderId(medicalOrderId)
        if (!heartRateList.isNullOrEmpty()) {
            val heartRateReport = HeartRateReport()
            heartRateList.forEach {
                if (it.value > 0) {
                    heartRateReport.list.add(it.value)
                    heartRateReport.total += it.value
                    heartRateReport.arv = heartRateReport.total / heartRateReport.list.size
                    heartRateReport.min = if (heartRateReport.min == -1) {
                        it.value
                    } else {
                        min(heartRateReport.min, it.value)
                    }
                    heartRateReport.max = max(heartRateReport.max, it.value)
                }
            }
            result.add(heartRateReport)
        }
        val shangXiaZhiList = shangXiaZhiRepository.getListByMedicalOrderId(medicalOrderId)
        if (!shangXiaZhiList.isNullOrEmpty()) {
            val shangXiaZhiReport = ShangXiaZhiReport()
            var isFirstSpasm = false// 是否第一次痉挛
            var mFirstSpasmValue = 0// 第一次痉挛值
            shangXiaZhiList.forEach {
                shangXiaZhiReport.count++
                // 速度
                shangXiaZhiReport.speedList.add(it.speedValue)
                shangXiaZhiReport.speedTotal += it.speedValue
                shangXiaZhiReport.speedArv = shangXiaZhiReport.speedTotal / shangXiaZhiReport.count
                shangXiaZhiReport.speedMin = if (shangXiaZhiReport.speedMin == -1) {
                    it.speedValue
                } else {
                    min(shangXiaZhiReport.speedMin, it.speedValue)
                }
                shangXiaZhiReport.speedMax = max(shangXiaZhiReport.speedMax, it.speedValue)
                //模式
                val resistance: Int
                if (it.model.toInt() == 0x01) {// 被动
                    resistance = 0
                    //被动里程
                    shangXiaZhiReport.passiveMil += it.speedValue * 0.5f * 1000 / 3600
                    //卡路里
                    shangXiaZhiReport.passiveCal += it.speedValue * 0.2f / 300
                } else {// 主动
                    resistance = it.res
                    //主动里程
                    shangXiaZhiReport.activeMil += it.speedValue * 0.5f * 1000 / 3600
                    //卡路里
                    shangXiaZhiReport.activeCal += it.speedValue * 0.2f * (resistance * 1.00f / 3.0f) / 60
                }
                // 阻力
                shangXiaZhiReport.resistanceTotal += resistance
                shangXiaZhiReport.resistanceArv = shangXiaZhiReport.resistanceTotal / shangXiaZhiReport.count
                shangXiaZhiReport.resistanceMin = if (shangXiaZhiReport.resistanceMin == -1) {
                    resistance
                } else {
                    min(shangXiaZhiReport.resistanceMin, resistance)
                }
                shangXiaZhiReport.resistanceMax = max(shangXiaZhiReport.resistanceMax, resistance)
                //偏差值：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
                val offset = it.offset - 15// 转换成游戏需要的 负数：左；0：不偏移；正数：右；
                // 转换成游戏需要的左边百分比 100~0
                val offsetValue = 100 - it.offset * 100 / 30
                //痉挛。注意：这里不直接使用 ShangXiaZhi 中的 spasmNum，是因为只要上下肢康复机不关机，那么它返回的痉挛次数值是一直累计的。
                if (it.spasmNum < 100) {
                    if (!isFirstSpasm) {
                        isFirstSpasm = true
                        mFirstSpasmValue = it.spasmNum
                    }
                    if (it.spasmNum - mFirstSpasmValue > shangXiaZhiReport.spasm) {
                        shangXiaZhiReport.spasm = it.spasmNum - mFirstSpasmValue
                    }
                }
                shangXiaZhiReport.spasmLevelTotal += it.spasmLevel
                shangXiaZhiReport.spasmLevelArv = shangXiaZhiReport.spasmLevelTotal / shangXiaZhiReport.count
                shangXiaZhiReport.spasmLevelMin = if (shangXiaZhiReport.spasmLevelMin == -1) {
                    it.spasmLevel
                } else {
                    min(shangXiaZhiReport.spasmLevelMin, it.spasmLevel)
                }
                shangXiaZhiReport.spasmLevelMax = max(shangXiaZhiReport.spasmLevelMax, it.spasmLevel)
            }
            result.add(shangXiaZhiReport)
        }
        return result
    }

}