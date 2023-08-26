package com.psk.device.data.source

import com.psk.device.DeviceType
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
class HeartRateRepository : IRepository<HeartRate>, KoinComponent {
    private lateinit var heartRateDbDataSource: HeartRateDbDataSource
    private lateinit var heartRateDataSource: BaseHeartRateDataSource

    override fun enable(name: String, address: String) {
        heartRateDbDataSource = get { parametersOf(DeviceType.HeartRate) }
        heartRateDataSource = get { parametersOf(name, DeviceType.HeartRate) }
        heartRateDataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            heartRateDataSource.fetch(medicalOrderId).collect {
                heartRateDbDataSource.save(it)
            }
        }
        return heartRateDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

}