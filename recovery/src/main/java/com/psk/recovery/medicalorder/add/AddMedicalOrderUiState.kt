package com.psk.recovery.medicalorder.add

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent

data class AddMedicalOrderUiState(
    val toastEvent: Event<ToastEvent>? = null,
)
