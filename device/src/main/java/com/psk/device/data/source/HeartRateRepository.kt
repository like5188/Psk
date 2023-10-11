package com.psk.device.data.source

import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.db.HeartRateDbDataSource
import com.psk.device.data.source.remote.ble.BleDataSourceFactory
import com.psk.device.data.source.remote.ble.base.BaseHeartRateDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 心率数据仓库
 */
@OptIn(KoinApiExtension::class)
class HeartRateRepository : KoinComponent, IRepository<HeartRate> {
    private val dbDataSource by lazy {
        HeartRateDbDataSource(get<DeviceDatabase>().heartRateDao())
    }
    private lateinit var dataSource: BaseHeartRateDataSource

    override fun enable(name: String, address: String) {
        dataSource = BleDataSourceFactory.create(name, DeviceType.HeartRate) as BaseHeartRateDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
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

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            dataSource.fetch(medicalOrderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    suspend fun fetch(): Flow<HeartRate> {
        return dataSource.fetch(-1)
    }

}