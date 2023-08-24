package com.psk.shangxiazhi.setting

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent

data class SettingUiState(
    val logout: Boolean = false,
    val toastEvent: Event<ToastEvent>? = null,
)