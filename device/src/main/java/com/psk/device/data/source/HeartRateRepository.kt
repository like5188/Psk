package com.psk.device.data.source

import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.remote.base.BaseHeartRateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/**
 * 心率数据仓库
 */
@OptIn(KoinApiExtension::class)
class HeartRateRepository : KoinComponent, BaseBleDeviceRepository<HeartRate, BaseHeartRateDataSource>(DeviceType.HeartRate) {

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            bleDeviceDataSource.fetch(medicalOrderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    suspend fun fetch(): Flow<HeartRate> {
        return bleDeviceDataSource.fetch(-1)
    }

}