package com.psk.shangxiazhi.login

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent

data class LoginUiState(
    val isLogin: Boolean? = null,
    val toastEvent: Event<ToastEvent>? = null,
)