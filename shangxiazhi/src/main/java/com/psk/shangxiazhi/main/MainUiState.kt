package com.psk.shangxiazhi.main

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event

data class MainUiState(
    val time: String = "",
    val isSplash: Boolean = true,// 是否处于闪屏界面（主题中利于属性 windowBackground 达到闪屏界面效果）
    val showLoginScreen: Boolean = false,
    val toastEvent: Event<ToastEvent>? = null,
)