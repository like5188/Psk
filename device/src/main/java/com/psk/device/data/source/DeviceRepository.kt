package com.psk.device.data.source

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.IBloodOxygenDataSource
import com.psk.device.data.source.remote.IBloodPressureDataSource
import com.psk.device.data.source.remote.IHeartRateDataSource
import com.psk.device.data.source.remote.IShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.XZX_ShangXiaZhiDataSource
import kotlinx.coroutines.flow.Flow

class DeviceRepository(
    private val bloodOxygenDbDataSource: BloodOxygenDbDataSource,
    private val bloodPressureDbDataSource: BloodPressureDbDataSource,
    private val heartRateDbDataSource: HeartRateDbDataSource,
    private val shangXiaZhiDbDataSource: ShangXiaZhiDbDataSource,
    private val bloodOxygenDataSource: IBloodOxygenDataSource,
    private val bloodPressureDataSource: IBloodPressureDataSource,
    private val heartRateDataSource: IHeartRateDataSource,
    private val shangXiaZhiDataSource: IShangXiaZhiDataSource,
    private val bleManager: BleManager
) {
    fun isAllDeviceConnected(): Boolean {
        return bleManager.isAllDeviceConnected()
    }

    fun connectAll(onConnected: (Device) -> Unit, onDisconnected: (Device) -> Unit) {
        bleManager.connectAll(true, onConnected, onDisconnected)
    }

    fun enableBloodOxygen() {
        bloodOxygenDataSource.enable()
    }

    fun enableBloodPressure() {
        bloodPressureDataSource.enable()
    }

    fun enableHeartRate() {
        heartRateDataSource.enable()
    }

    fun enableShangXiaZhi() {
        shangXiaZhiDataSource.enable()
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
        medicalOrderId: Long,
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onOver: (() -> Unit)? = null
    ) {
        if (shangXiaZhiDataSource is XZX_ShangXiaZhiDataSource) {
            shangXiaZhiDataSource.onStart = onStart
            shangXiaZhiDataSource.onPause = onPause
            shangXiaZhiDataSource.onOver = onOver
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
