package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.local.db.BloodOxygenDbDataSource
import com.psk.device.data.source.remote.BaseBloodOxygenDataSource
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
 * 血氧数据仓库
 */
@OptIn(KoinApiExtension::class)
class BloodOxygenRepository : KoinComponent, IRepository {
    private lateinit var bloodOxygenDbDataSource: BloodOxygenDbDataSource
    private lateinit var bloodOxygenDataSource: BaseBloodOxygenDataSource

    fun enable(name: String, address: String) {
        bloodOxygenDbDataSource = get { parametersOf(DeviceType.BloodOxygen) }
        bloodOxygenDataSource = get { parametersOf(name, DeviceType.BloodOxygen) }
        bloodOxygenDataSource.enable(address)
    }

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long = 1000): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bloodOxygenDataSource.fetch(medicalOrderId)?.apply {
                    bloodOxygenDbDataSource.save(this)
                }
                delay(interval)
            }
        }
        return bloodOxygenDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

}