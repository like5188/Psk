package com.psk.device.data.source

import com.psk.ble.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.local.db.IDbDataSource
import com.psk.device.data.source.local.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.BaseRemoteDeviceDataSource
import com.psk.device.data.source.remote.BaseShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
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
 * 上下肢数据仓库
 */
@OptIn(KoinApiExtension::class)
class ShangXiaZhiRepository : KoinComponent, IRepository<ShangXiaZhi> {
    private val dbDataSource: ShangXiaZhiDbDataSource by lazy {
        get<IDbDataSource<*>> { parametersOf(DeviceType.ShangXiaZhi) } as ShangXiaZhiDbDataSource
    }
    private lateinit var dataSource: BaseShangXiaZhiDataSource

    override fun enable(name: String, address: String) {
        dataSource = get<BaseRemoteDeviceDataSource> { parametersOf(name, DeviceType.ShangXiaZhi) } as BaseShangXiaZhiDataSource
        dataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<ShangXiaZhi> {
        scope.launch(Dispatchers.IO) {
            dataSource.fetch(medicalOrderId).collect {
                dbDataSource.insert(it)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    fun setCallback(
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onOver: (() -> Unit)? = null,
    ) {
        (dataSource as? RKF_ShangXiaZhiDataSource)?.apply {
            this.onStart = onStart
            this.onPause = onPause
            this.onOver = onOver
        }
    }

    suspend fun resume() {
        dataSource.resume()
    }

    suspend fun pause() {
        dataSource.pause()
    }

    suspend fun over() {
        dataSource.over()
    }

    suspend fun setParams(params: ShangXiaZhiParams) {
        dataSource.setParams(params)
    }
}