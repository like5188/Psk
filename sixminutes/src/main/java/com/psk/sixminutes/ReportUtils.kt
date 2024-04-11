package com.psk.sixminutes

import android.content.Context
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.repository.ble.BloodOxygenRepository
import com.psk.device.data.source.repository.ble.HeartRateRepository
import com.psk.sixminutes.data.db.SixMinutesDatabaseManager
import com.psk.sixminutes.data.source.HealthInfoRepository

class ReportUtils private constructor() {
    private val bloodOxygenRepository = DeviceRepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private val heartRateRepository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private val healthInfoRepository: HealthInfoRepository by lazy {
        HealthInfoRepository()
    }

    companion object {
        @JvmStatic
        fun getInstance(context: Context): ReportUtils {
            // 只需要初始化数据库相关，不需要使用DeviceRepositoryManager.init(context.applicationContext)
            DeviceDatabaseManager.init(context.applicationContext)
            SixMinutesDatabaseManager.init(context.applicationContext)
            return Holder.instance
        }
    }

    private object Holder {
        val instance = ReportUtils()
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