package com.psk.device.data.source

import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.remote.base.BaseBloodOxygenDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/**
 * 血氧数据仓库
 */
@OptIn(KoinApiExtension::class)
class BloodOxygenRepository : KoinComponent, BaseBleDeviceRepository<BloodOxygen, BaseBloodOxygenDataSource>(DeviceType.BloodOxygen) {

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bleDeviceDataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.insert(this)
                }
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

}