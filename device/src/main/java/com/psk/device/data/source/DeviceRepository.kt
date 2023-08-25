package com.psk.device.data.source

import com.psk.device.DeviceType
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.BaseBloodOxygenDataSource
import com.psk.device.data.source.remote.BaseBloodPressureDataSource
import com.psk.device.data.source.remote.BaseHeartRateDataSource
import com.psk.device.data.source.remote.BaseShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
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
 * 蓝牙设备数据仓库
 * 注意：如果要添加新的蓝牙设备，那么需要一下步骤：
 * 1、新增一个 [扫描出来的蓝牙设备的名称前缀]_[DeviceType]Datasource 到 [com.psk.device.data.source.remote.ble] 中，注意命名格式和位置。
 * 2、在本仓库中添加自己想要的方法。
 */
@OptIn(KoinApiExtension::class)
class DeviceRepository : KoinComponent {
    private lateinit var bloodOxygenDbDataSource: BloodOxygenDbDataSource
    private lateinit var bloodPressureDbDataSource: BloodPressureDbDataSource
    private lateinit var heartRateDbDataSource: HeartRateDbDataSource
    private lateinit var shangXiaZhiDbDataSource: ShangXiaZhiDbDataSource
    private lateinit var bloodOxygenDataSource: BaseBloodOxygenDataSource
    private lateinit var bloodPressureDataSource: BaseBloodPressureDataSource
    private lateinit var heartRateDataSource: BaseHeartRateDataSource
    private lateinit var shangXiaZhiDataSource: BaseShangXiaZhiDataSource

    fun enableBloodOxygen(name: String, address: String) {
        bloodOxygenDbDataSource = get()
        bloodOxygenDataSource = get { parametersOf(name, DeviceType.BloodOxygen) }
        bloodOxygenDataSource.enable(address)
    }

    fun enableBloodPressure(name: String, address: String) {
        bloodPressureDbDataSource = get()
        bloodPressureDataSource = get { parametersOf(name, DeviceType.BloodPressure) }
        bloodPressureDataSource.enable(address)
    }

    fun enableHeartRate(name: String, address: String) {
        heartRateDbDataSource = get()
        heartRateDataSource = get { parametersOf(name, DeviceType.HeartRate) }
        heartRateDataSource.enable(address)
    }

    fun enableShangXiaZhi(name: String, address: String) {
        shangXiaZhiDbDataSource = get()
        shangXiaZhiDataSource = get { parametersOf(name, DeviceType.ShangXiaZhi) }
        shangXiaZhiDataSource.enable(address)
    }

    suspend fun getBloodOxygenListByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getBloodPressureListByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getHeartRateListByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getShangXiaZhiListByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    fun getBloodOxygenFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long = 1000): Flow<BloodOxygen> {
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

    fun getBloodPressureFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long = 1000): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bloodPressureDataSource.fetch(medicalOrderId)?.apply {
                    bloodPressureDbDataSource.save(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return bloodPressureDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

    fun getHeartRateFlow(scope: CoroutineScope, medicalOrderId: Long): Flow<HeartRate> {
        scope.launch(Dispatchers.IO) {
            heartRateDataSource.fetch(medicalOrderId).collect {
                heartRateDbDataSource.save(it)
            }
        }
        return heartRateDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
    }

    fun getShangXiaZhiFlow(
        scope: CoroutineScope,
        medicalOrderId: Long,
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onOver: (() -> Unit)? = null,
    ): Flow<ShangXiaZhi> {
        scope.launch(Dispatchers.IO) {
            (shangXiaZhiDataSource as? RKF_ShangXiaZhiDataSource)?.apply {
                this.onStart = onStart
                this.onPause = onPause
                this.onOver = onOver
            }
            shangXiaZhiDataSource.fetch(medicalOrderId).collect {
                shangXiaZhiDbDataSource.save(it)
            }
        }
        return shangXiaZhiDbDataSource.listenLatest(System.currentTimeMillis() / 1000)
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
