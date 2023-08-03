package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.MonitorDeviceDao
import com.psk.recovery.data.model.MonitorDevice

class MonitorDeviceDbDataSource(
    private val monitorDeviceDao: MonitorDeviceDao
) {

    suspend fun save(vararg monitorDevices: MonitorDevice) {
        monitorDeviceDao.insert(*monitorDevices)
    }

}
