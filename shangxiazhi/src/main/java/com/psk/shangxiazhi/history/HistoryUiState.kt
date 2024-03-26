package com.psk.shangxiazhi.history

import com.psk.shangxiazhi.data.model.OrderInfo

data class HistoryUiState(
    val showTime: String = "",
    val orderInfoList: List<OrderInfo>? = null,
)