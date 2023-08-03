package com.psk.recovery.data.source.db

import com.like.paging.RequestType
import com.like.paging.dataSource.byPageNoKeyed.PageNoKeyedPagingDataSource
import com.psk.recovery.data.db.dao.MedicalOrderDao
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice

class MedicalOrderAndMonitorDevicesDbDataSource(
    private val medicalOrderDao: MedicalOrderDao
) : PageNoKeyedPagingDataSource<List<MedicalOrderAndMonitorDevice>?>(1, pageSize = 10) {
    private var status: Int = 3

    /**
     * @param status    类型。
     */
    fun setParams(status: Int) {
        this.status = status
    }

    override suspend fun load(requestType: RequestType, pageNo: Int, pageSize: Int): List<MedicalOrderAndMonitorDevice>? {
        if (status < 0 || status > 3) {
            throw IllegalArgumentException("status is invalid")
        }
        return medicalOrderDao.getMedicalOrderAndMonitorDeviceListByStatus(status, pageNo, pageSize)
    }

}
