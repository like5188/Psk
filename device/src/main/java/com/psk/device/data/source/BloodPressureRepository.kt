package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.local.db.BloodPressureDbDataSource
import com.psk.device.data.source.local.db.IDbDataSource
import com.psk.device.data.source.remote.BaseBloodPressureDataSource
import com.psk.device.data.source.remote.BaseRemoteDeviceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
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
class BloodPressureRepository : KoinComponent, IRepository<BloodPressure> {
    private val dbDataSource: BloodPressureDbDataSource by lazy {
        get<IDbDataSource<*>> { parametersOf(DeviceType.BloodPressure) } as BloodPressureDbDataSource
    }
    private lateinit var dataSource: BaseBloodPressureDataSource

    override fun enable(name: String, address: String) {
        dataSource = get<BaseRemoteDeviceDataSource> { parametersOf(name, DeviceType.BloodPressure) } as BaseBloodPressureDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFetchFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                dataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.insert(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000).filterNotNull()
    }

    fun getMeasureFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                dataSource.measure(medicalOrderId)?.apply {
                    dbDataSource.insert(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000).filterNotNull()
    }

    suspend fun measure(): BloodPressure? {
        return dataSource.measure(-1)
    }
}