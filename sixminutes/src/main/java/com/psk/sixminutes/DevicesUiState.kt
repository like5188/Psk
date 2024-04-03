package com.psk.sixminutes

import com.like.common.util.mvi.Event
import com.like.common.util.ToastEvent

data class DevicesUiState(
    val date: String = "",
    val toastEvent: Event<ToastEvent>? = null,
)