package com.psk.recovery.medicalorder.list

import androidx.lifecycle.ViewModel
import com.psk.recovery.data.source.RecoveryRepository

class MedicalOrderListViewModel(
    private val recoveryRepository: RecoveryRepository,
) : ViewModel() {

    fun getMedicalOrderAndMonitorDevicesResult(status: Int) = recoveryRepository.getMedicalOrderAndMonitorDevicesResult(status)

}
