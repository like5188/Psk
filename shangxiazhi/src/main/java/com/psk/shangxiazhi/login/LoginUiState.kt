package com.psk.shangxiazhi.login

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event

data class LoginUiState(
    val isLogin: Boolean? = null,
    val serialNumber: String = "",
    val toastEvent: Event<ToastEvent>? = null,
)