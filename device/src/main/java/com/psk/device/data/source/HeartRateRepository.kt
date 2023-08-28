package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.IDbDataSource
import com.psk.device.data.source.local.db.HeartRateDbDataSource
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import com.psk.device.data.source.remote.BaseRemoteDeviceDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * 心率数据仓库
 */
@OptIn(KoinApiExtension::class)
class HeartRateRepository : KoinComponent, IRepository<HeartRate> {
    private lateinit var dbDataSource: HeartRateDbDataSource
    private lateinit var dataSource: BaseHeartRateDataSource

    override fun enable(name: String, address: String) {
        dbDataSource = get<IDbDataSource<*>> { parametersOf(DeviceType.HeartRate) } as HeartRateDbDataSource
        dataSource = get<BaseRemoteDeviceDataSource> { parametersOf(name, DeviceType.HeartRate) } as BaseHeartRateDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            dataSource.fetch(medicalOrderId).collect {
                dbDataSource.save(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000).filterNotNull()
    }

}