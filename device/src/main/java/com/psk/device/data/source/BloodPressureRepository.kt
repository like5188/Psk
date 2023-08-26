package com.psk.device.data.source

import com.psk.device.DeviceType
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.local.db.BloodPressureDbDataSource
import com.psk.device.data.source.remote.BaseBloodPressureDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * 血压数据仓库
 */
@OptIn(KoinApiExtension::class)
class BloodPressureRepository : IRepository<BloodPressure>, KoinComponent {
    private lateinit var bloodPressureDbDataSource: BloodPressureDbDataSource
    private lateinit var bloodPressureDataSource: BaseBloodPressureDataSource

    override fun enable(name: String, address: String) {
        bloodPressureDbDataSource = get { parametersOf(DeviceType.BloodPressure) }
        bloodPressureDataSource = get { parametersOf(name, DeviceType.BloodPressure) }
        bloodPressureDataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bloodPressureDataSource.fetch(medicalOrderId)?.apply {
                    bloodPressureDbDataSource.save(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return bloodPressureDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

}