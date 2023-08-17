package com.psk.recovery.medicalorder.execute

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.seeker.luckychart.model.ECGPointValue

data class ExecuteMedicalOrderUiState(
    val bloodOxygen: BloodOxygen? = null,
    val bloodPressure: BloodPressure? = null,
    val heartRate: Int? = null,
    val coorYArray: FloatArray? = null,
    val time: String = "00:00",
    val startOrPause: String = "开始",
    val startOrPauseEnable: Boolean = true,
    val toastEvent: Event<ToastEvent>? = null,
)
