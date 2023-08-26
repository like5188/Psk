package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.db.HeartRateDbDataSource
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * 心率数据仓库
 */
@OptIn(KoinApiExtension::class)
class HeartRateRepository : KoinComponent, IRepository {
    private lateinit var dbDataSource: HeartRateDbDataSource
    private lateinit var dataSource: BaseHeartRateDataSource

    fun enable(name: String, address: String) {
        dbDataSource = get { parametersOf(DeviceType.HeartRate) }
        dataSource = get { parametersOf(name, DeviceType.HeartRate) }
        dataSource.enable(address)
    }

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            dataSource.fetch(medicalOrderId).collect {
                dbDataSource.save(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

}