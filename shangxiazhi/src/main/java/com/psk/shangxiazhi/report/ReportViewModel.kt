package com.psk.shangxiazhi.report

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

class ReportViewModel : ViewModel() {
    private val bloodOxygenRepository = RepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository = RepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = RepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val shangXiaZhiRepository = RepositoryManager.createBleDeviceRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi)
    private val healthInfoRepository = RepositoryManager.healthInfoRepository

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