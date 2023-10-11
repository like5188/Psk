package com.psk.device.data.source

import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.local.db.HeartRateDbDataSource
import com.psk.device.data.source.local.db.IDbDataSource
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import com.psk.device.data.source.remote.BaseBleDeviceDataSource
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
    private val dbDataSource: HeartRateDbDataSource by lazy {
        get<IDbDataSource<*>> { parametersOf(DeviceType.HeartRate) } as HeartRateDbDataSource
    }
    private lateinit var dataSource: BaseHeartRateDataSource

    override fun enable(name: String, address: String) {
        dataSource = get<BaseBleDeviceDataSource> { parametersOf(name, DeviceType.HeartRate) } as BaseHeartRateDataSource
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