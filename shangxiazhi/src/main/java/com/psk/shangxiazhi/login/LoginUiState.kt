package com.psk.shangxiazhi.login

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.data.model.Login

data class LoginUiState(
    val login: Login? = null,
    val toastEvent: Event<ToastEvent>? = null,
)