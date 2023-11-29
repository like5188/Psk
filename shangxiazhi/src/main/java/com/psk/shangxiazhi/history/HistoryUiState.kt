package com.psk.shangxiazhi.history

import com.psk.device.data.model.Order

data class HistoryUiState(
    val showTime: String? = null,
    val orderList: List<Order>? = null,
)