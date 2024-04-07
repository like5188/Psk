package com.psk.sixminutes

import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.repository.ble.BloodOxygenRepository
import com.psk.device.data.source.repository.ble.BloodPressureRepository
import com.psk.device.data.source.repository.ble.HeartRateRepository
import com.psk.sixminutes.data.source.HealthInfoRepository

class ReportUtils {
    private val bloodOxygenRepository = DeviceRepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val bloodPressureRepository =
        DeviceRepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private val heartRateRepository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val healthInfoRepository: HealthInfoRepository by lazy {
        HealthInfoRepository()
    }

    suspend fun getBloodOxygenListByOrderId(orderId: Long): List<BloodOxygen>? {
        return bloodOxygenRepository.getListByOrderId(orderId)
    }

    suspend fun getHeartRateListByOrderId(orderId: Long): List<HeartRate>? {
        return heartRateRepository.getListByOrderId(orderId)
    }

    suspend fun getBloodPressureBeforeByOrderId(orderId: Long): BloodPressure? {
        return healthInfoRepository.getByOrderId(orderId)?.bloodPressureBefore
    }

    suspend fun getBloodPressureAfterByOrderId(orderId: Long): BloodPressure? {
        return healthInfoRepository.getByOrderId(orderId)?.bloodPressureAfter
    }

}