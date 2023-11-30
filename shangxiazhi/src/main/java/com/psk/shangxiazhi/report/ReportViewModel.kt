package com.psk.shangxiazhi.report

import androidx.lifecycle.ViewModel
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.device.data.source.BloodPressureRepository
import com.psk.device.data.source.HeartRateRepository
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.BloodPressureReport
import com.psk.shangxiazhi.data.model.HealthInfo
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.data.source.HealthInfoRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

class ReportViewModel(
    private val healthInfoRepository: HealthInfoRepository,
) : ViewModel() {
    private val bloodOxygenRepository = DeviceRepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository =
        DeviceRepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val shangXiaZhiRepository = DeviceRepositoryManager.createBleDeviceRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi)

    suspend fun getReports(orderId: Long): List<IReport> {
        val result = mutableListOf<IReport>()
        bloodOxygenRepository.getListByOrderId(orderId)?.let {
            BloodOxygenReport.createForm(it.asFlow())
            result.add(BloodOxygenReport.report)
        }
        bloodPressureRepository.getListByOrderId(orderId)?.let {
            BloodPressureReport.createForm(it.asFlow())
            result.add(BloodPressureReport.report)
        }
        heartRateRepository.getListByOrderId(orderId)?.let {
            HeartRateReport.createForm(it.asFlow())
            result.add(HeartRateReport.report)
        }
        shangXiaZhiRepository.getListByOrderId(orderId)?.let {
            ShangXiaZhiReport.createForm(it.asFlow()).collect()
            result.add(ShangXiaZhiReport.report)
        }
        return result
    }

    suspend fun getHealthInfo(orderId: Long): HealthInfo? {
        return healthInfoRepository.getByOrderId(orderId)
    }

}