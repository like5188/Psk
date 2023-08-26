package com.psk.device.data.source

import com.psk.device.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.local.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.BaseShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * 上下肢数据仓库
 */
@OptIn(KoinApiExtension::class)
class ShangXiaZhiRepository : IRepository<ShangXiaZhi>, KoinComponent {
    private lateinit var shangXiaZhiDbDataSource: ShangXiaZhiDbDataSource
    private lateinit var shangXiaZhiDataSource: BaseShangXiaZhiDataSource

    override fun enable(name: String, address: String) {
        shangXiaZhiDbDataSource = get { parametersOf(DeviceType.ShangXiaZhi) }
        shangXiaZhiDataSource = get { parametersOf(name, DeviceType.ShangXiaZhi) }
        shangXiaZhiDataSource.enable(address)
    }

    override suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    override fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long): Flow<ShangXiaZhi> {
        scope.launch(Dispatchers.IO) {
            shangXiaZhiDataSource.fetch(medicalOrderId).collect {
                shangXiaZhiDbDataSource.save(it)
            }
        }
        return shangXiaZhiDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

    fun setCallback(
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onOver: (() -> Unit)? = null,
    ) {
        (shangXiaZhiDataSource as? RKF_ShangXiaZhiDataSource)?.apply {
            this.onStart = onStart
            this.onPause = onPause
            this.onOver = onOver
        }
    }

    suspend fun resumeShangXiaZhi() {
        shangXiaZhiDataSource.resume()
    }

    suspend fun pauseShangXiaZhi() {
        shangXiaZhiDataSource.pause()
    }

    suspend fun overShangXiaZhi() {
        shangXiaZhiDataSource.over()
    }

    /**
     * @param passiveModule     被动模式
     * @param timeInt           时间 5-30 min
     * @param speedInt          速度 5-60 rpm
     * @param spasmInt          痉挛等级 1-12
     * @param resistanceInt     阻力 1-12
     * @param intelligent       智能模式
     * @param turn2             正转
     */
    suspend fun setShangXiaZhiParams(
        passiveModule: Boolean, timeInt: Int, speedInt: Int, spasmInt: Int, resistanceInt: Int, intelligent: Boolean, turn2: Boolean
    ) {
        shangXiaZhiDataSource.setParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
    }
}