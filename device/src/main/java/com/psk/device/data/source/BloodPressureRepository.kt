package com.psk.device.data.source

import com.psk.ble.DeviceType
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
class BloodPressureRepository : KoinComponent, IRepository {
    private lateinit var dbDataSource: BloodPressureDbDataSource
    private lateinit var dataSource: BaseBloodPressureDataSource

    fun enable(name: String, address: String) {
        dbDataSource = get { parametersOf(DeviceType.BloodPressure) }
        dataSource = get { parametersOf(name, DeviceType.BloodPressure) }
        dataSource.enable(address)
    }

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long = 1000): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                dataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.save(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

}