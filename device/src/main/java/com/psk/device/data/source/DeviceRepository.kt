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
import com.psk.device.data.source.remote.BleDeviceDataSourceFactory
import com.psk.device.data.source.remote.ble.RKF_ShangXiaZhiDataSource
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/**
 * 蓝牙设备数据仓库
 * 注意：如果要添加新的蓝牙设备，那么需要一下步骤：
 * 1、新增一个 [扫描出来的蓝牙设备的名称前缀]_[DeviceType]Datasource 到 [com.psk.device.data.source.remote.ble] 中，注意命名格式和位置。
 * 2、在本仓库中添加自己想要的方法。
 */
@OptIn(KoinApiExtension::class)
class DeviceRepository(
    private val bloodOxygenDbDataSource: BloodOxygenDbDataSource,
    private val bloodPressureDbDataSource: BloodPressureDbDataSource,
    private val heartRateDbDataSource: HeartRateDbDataSource,
    private val shangXiaZhiDbDataSource: ShangXiaZhiDbDataSource,
) : KoinComponent {
    private lateinit var bloodOxygenDataSource: BaseBloodOxygenDataSource
    private lateinit var bloodPressureDataSource: BaseBloodPressureDataSource
    private lateinit var heartRateDataSource: BaseHeartRateDataSource
    private lateinit var shangXiaZhiDataSource: BaseShangXiaZhiDataSource

    fun enableBloodOxygen(name: String, address: String) {
        bloodOxygenDataSource = BleDeviceDataSourceFactory.create(name, DeviceType.BloodOxygen) as BaseBloodOxygenDataSource
        bloodOxygenDataSource.enable(address)
    }

    fun enableBloodPressure(name: String, address: String) {
        bloodPressureDataSource = BleDeviceDataSourceFactory.create(name, DeviceType.BloodPressure) as BaseBloodPressureDataSource
        bloodPressureDataSource.enable(address)
    }

    fun enableHeartRate(name: String, address: String) {
        heartRateDataSource = BleDeviceDataSourceFactory.create(name, DeviceType.HeartRate) as BaseHeartRateDataSource
        heartRateDataSource.enable(address)
    }

    fun enableShangXiaZhi(name: String, address: String) {
        shangXiaZhiDataSource = BleDeviceDataSourceFactory.create(name, DeviceType.ShangXiaZhi) as BaseShangXiaZhiDataSource
        shangXiaZhiDataSource.enable(address)
    }

    fun listenLatestBloodOxygen(startTime: Long): Flow<BloodOxygen> {
        return bloodOxygenDbDataSource.listenLatest(startTime)
    }

    fun listenLatestBloodPressure(startTime: Long): Flow<BloodPressure> {
        return bloodPressureDbDataSource.listenLatest(startTime)
    }

    fun listenLatestHeartRate(startTime: Long): Flow<HeartRate> {
        return heartRateDbDataSource.listenLatest(startTime)
    }

    fun listenLatestShangXiaZhi(startTime: Long): Flow<ShangXiaZhi> {
        return shangXiaZhiDbDataSource.listenLatest(startTime)
    }

    suspend fun getBloodOxygenByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getBloodPressureByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getHeartRateByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun getShangXiaZhiByMedicalOrderId(medicalOrderId: Long): List<ShangXiaZhi>? {
        return shangXiaZhiDbDataSource.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun fetchBloodOxygenAndSave(medicalOrderId: Long) {
        bloodOxygenDataSource.fetch(medicalOrderId)?.apply {
            bloodOxygenDbDataSource.save(this)
        }
    }

    suspend fun fetchBloodPressureAndSave(medicalOrderId: Long) {
        bloodPressureDataSource.fetch(medicalOrderId)?.apply {
            bloodPressureDbDataSource.save(this)
        }
    }

    suspend fun fetchHeartRateAndSave(medicalOrderId: Long) {
        heartRateDataSource.fetch(medicalOrderId).collect {
            heartRateDbDataSource.save(it)
        }
    }

    suspend fun fetchShangXiaZhiAndSave(
        medicalOrderId: Long, onStart: (() -> Unit)? = null, onPause: (() -> Unit)? = null, onOver: (() -> Unit)? = null
    ) {
        (shangXiaZhiDataSource as? RKF_ShangXiaZhiDataSource)?.apply {
            this.onStart = onStart
            this.onPause = onPause
            this.onOver = onOver
        }
        shangXiaZhiDataSource.fetch(medicalOrderId).collect {
            shangXiaZhiDbDataSource.save(it)
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
