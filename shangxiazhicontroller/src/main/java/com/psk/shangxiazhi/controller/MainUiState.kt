package com.psk.shangxiazhi.controller

import com.psk.device.data.model.ShangXiaZhi

data class MainUiState(
    val name: String = "",
    val connectState: String = "",
    val isRunning: Boolean = false,
    val shangXiaZhi: ShangXiaZhi? = null,
)