package com.psk.device.data.source

import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.local.db.BloodOxygenDbDataSource
import com.psk.device.data.source.remote.ble.BleDataSourceFactory
import com.psk.device.data.source.remote.ble.base.BaseBloodOxygenDataSource
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

/**
 * 血氧数据仓库
 */
@OptIn(KoinApiExtension::class)
class BloodOxygenRepository : KoinComponent, IBleDeviceRepository<BloodOxygen> {
    private val dbDataSource by lazy {
        BloodOxygenDbDataSource(get<DeviceDatabase>().bloodOxygenDao())
    }
    private lateinit var dataSource: BaseBloodOxygenDataSource

    override fun enable(name: String, address: String) {
        dataSource = BleDataSourceFactory.create(name, DeviceType.BloodOxygen) as BaseBloodOxygenDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun connect(scope: CoroutineScope, onConnected: () -> Unit, onDisconnected: () -> Unit) {
        dataSource.connect(scope, onConnected, onDisconnected)
    }

    override fun isConnected(): Boolean {
        return dataSource.isConnected()
    }

    override fun close() {
        dataSource.close()
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<BloodOxygen> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                dataSource.fetch(medicalOrderId)?.apply {
                    dbDataSource.insert(this)
                }
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

}