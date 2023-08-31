package com.psk.shangxiazhi.report

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent

data class ReportUiState(
    val toastEvent: Event<ToastEvent>? = null,
)