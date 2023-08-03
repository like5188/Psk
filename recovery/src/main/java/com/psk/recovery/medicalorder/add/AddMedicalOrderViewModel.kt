package com.psk.recovery.medicalorder.add

import androidx.lifecycle.ViewModel
import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.data.source.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddMedicalOrderViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddMedicalOrderUiState())
    val uiState = _uiState.asStateFlow()

    suspend fun addMedicalOrder(medicalOrder: MedicalOrder): Long {
        return deviceRepository.saveMedicalOrder(medicalOrder)
    }

    suspend fun addMonitorDevices(vararg monitorDevices: MonitorDevice) {
        deviceRepository.saveMonitorDevices(*monitorDevices)
        _uiState.update {
            it.copy(toastEvent = Event(ToastEvent(text = "新增医嘱成功")))
        }
    }

}
