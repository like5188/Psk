package com.psk.shangxiazhi.history

import com.psk.device.data.model.OrderInfo

data class HistoryUiState(
    val showTime: String? = null,
    val orderInfoList: List<OrderInfo>? = null,
)