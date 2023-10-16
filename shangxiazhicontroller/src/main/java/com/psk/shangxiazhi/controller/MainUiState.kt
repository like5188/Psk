package com.psk.shangxiazhi.controller

import com.psk.device.data.model.ShangXiaZhi

data class MainUiState(
    val name: String = "",
    val isConnected: Boolean = false,
    val connectState: String = "",
    val shangXiaZhi: ShangXiaZhi? = null,
    val isRunning: Boolean = false
)