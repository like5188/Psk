package com.psk.shangxiazhi.main

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.game.GameManagerService

data class MainUiState(
    val time: String = "",
    val userName: String = "",
    val gameManagerService: GameManagerService? = null,
    val toastEvent: Event<ToastEvent>? = null,
)