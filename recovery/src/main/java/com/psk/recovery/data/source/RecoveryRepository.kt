package com.psk.recovery.data.source

import com.like.paging.PagingResult
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.data.source.db.MedicalOrderAndMonitorDevicesDbDataSource
import com.psk.recovery.data.source.db.MedicalOrderDbDataSource
import com.psk.recovery.data.source.db.MonitorDeviceDbDataSource

class RecoveryRepository(
    private val medicalOrderDbDataSource: MedicalOrderDbDataSource,
    private val monitorDeviceDbDataSource: MonitorDeviceDbDataSource,
    private val medicalOrderAndMonitorDevicesDbDataSource: MedicalOrderAndMonitorDevicesDbDataSource,
) {

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
