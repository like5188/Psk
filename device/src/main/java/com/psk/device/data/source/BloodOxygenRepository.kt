package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.local.IDbDataSource
import com.psk.device.data.source.local.db.BloodOxygenDbDataSource
import com.psk.device.data.source.remote.BaseBloodOxygenDataSource
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
 * 血氧数据仓库
 */
@OptIn(KoinApiExtension::class)
class BloodOxygenRepository : KoinComponent, IRepository<BloodOxygen> {
    private lateinit var dbDataSource: BloodOxygenDbDataSource
    private lateinit var dataSource: BaseBloodOxygenDataSource

    override fun enable(name: String, address: String) {
        dbDataSource = get<IDbDataSource<*>> { parametersOf(DeviceType.BloodOxygen) } as BloodOxygenDbDataSource
        dataSource = get<BaseRemoteDeviceDataSource> { parametersOf(name, DeviceType.BloodOxygen) } as BaseBloodOxygenDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                dataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.save(this)
                }
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis() / 1000).filterNotNull()
    }

}