package com.psk.recovery.data.source

import com.like.paging.PagingResult
import com.psk.recovery.data.model.BloodOxygen
import com.psk.recovery.data.model.BloodPressure
import com.psk.recovery.data.model.HeartRate
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.data.source.db.BloodOxygenDbDataSource
import com.psk.recovery.data.source.db.BloodPressureDbDataSource
import com.psk.recovery.data.source.db.HeartRateDbDataSource
import com.psk.recovery.data.source.db.MedicalOrderAndMonitorDevicesDbDataSource
import com.psk.recovery.data.source.db.MedicalOrderDbDataSource
import com.psk.recovery.data.source.db.MonitorDeviceDbDataSource
import com.psk.recovery.data.source.db.ShangXiaZhiDbDataSource
import com.psk.recovery.data.source.remote.IBloodOxygenDataSource
import com.psk.recovery.data.source.remote.IBloodPressureDataSource
import com.psk.recovery.data.source.remote.IHeartRateDataSource
import com.psk.recovery.data.source.remote.IShangXiaZhiDataSource
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@OptIn(KoinApiExtension::class)
class DeviceRepository(
    private val bloodOxygenDbDataSource: BloodOxygenDbDataSource,
    private val bloodPressureDbDataSource: BloodPressureDbDataSource,
    private val heartRateDbDataSource: HeartRateDbDataSource,
    private val shangXiaZhiDbDataSource: ShangXiaZhiDbDataSource,
    private val medicalOrderDbDataSource: MedicalOrderDbDataSource,
    private val monitorDeviceDbDataSource: MonitorDeviceDbDataSource,
    private val medicalOrderAndMonitorDevicesDbDataSource: MedicalOrderAndMonitorDevicesDbDataSource,
) : KoinComponent {
    //    private val bloodOxygenDataSource: IBloodOxygenDataSource = get(named("mock"))
//    private val bloodPressureDataSource: IBloodPressureDataSource = get(named("mock"))
//    private val heartRateDataSource: IHeartRateDataSource = get(named("mock"))
    private val bloodOxygenDataSource: IBloodOxygenDataSource = get(named("O2"))
    private val bloodPressureDataSource: IBloodPressureDataSource = get(named("BP88B180704"))

    //    private val heartRateDataSource: IHeartRateDataSource = get(named("SCI311W"))
    private val heartRateDataSource: IHeartRateDataSource = get(named("ER1"))
    private val shangXiaZhiDataSource: IShangXiaZhiDataSource = get(named("XZX"))

    fun connectBloodOxygen(
        onConnected: () -> Unit,
        onDisconnected: (() -> Unit)? = null,
    ) {
        bloodOxygenDataSource.connect(onConnected, onDisconnected)
    }

    fun connectBloodPressure(
        onConnected: () -> Unit,
        onDisconnected: (() -> Unit)? = null,
    ) {
        bloodPressureDataSource.connect(onConnected, onDisconnected)
    }

    fun connectHeartRate(
        onConnected: () -> Unit,
        onDisconnected: (() -> Unit)? = null,
    ) {
        heartRateDataSource.connect(onConnected, onDisconnected)
    }

    fun connectShangXiaZhi(
        onConnected: () -> Unit,
        onDisconnected: (() -> Unit)? = null,
    ) {
        shangXiaZhiDataSource.connect(onConnected, onDisconnected)
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

    suspend fun fetchShangXiaZhiAndSave(medicalOrderId: Long) {
        shangXiaZhiDataSource.fetch(medicalOrderId).collect {
            shangXiaZhiDbDataSource.save(it)
        }
    }

    suspend fun saveMedicalOrder(medicalOrder: MedicalOrder): Long {
        return medicalOrderDbDataSource.save(medicalOrder)
    }

    suspend fun saveMonitorDevices(vararg monitorDevices: MonitorDevice) {
        monitorDeviceDbDataSource.save(*monitorDevices)
    }

    fun getMedicalOrderAndMonitorDevicesResult(status: Int): PagingResult<List<MedicalOrderAndMonitorDevice>?> {
        medicalOrderAndMonitorDevicesDbDataSource.setParams(status)
        return medicalOrderAndMonitorDevicesDbDataSource.pagingResult()
    }

    suspend fun updateMedicalOrders(vararg medicalOrders: MedicalOrder): Int {
        return medicalOrderDbDataSource.update(*medicalOrders)
    }

}
