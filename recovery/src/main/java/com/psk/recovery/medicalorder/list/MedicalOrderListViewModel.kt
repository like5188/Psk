package com.psk.recovery.medicalorder.list

import androidx.lifecycle.ViewModel
import com.psk.recovery.data.source.DeviceRepository

class MedicalOrderListViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    fun getMedicalOrderAndMonitorDevicesResult(status: Int) = deviceRepository.getMedicalOrderAndMonitorDevicesResult(status)

}
