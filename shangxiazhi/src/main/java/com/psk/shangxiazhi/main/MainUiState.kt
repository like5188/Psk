package com.psk.shangxiazhi.main

import com.like.common.util.mvi.Event
import com.like.common.util.ToastEvent

data class MainUiState(
    val time: String = "",
    val toastEvent: Event<ToastEvent>? = null,
)