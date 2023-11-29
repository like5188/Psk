package com.psk.shangxiazhi.history

data class HistoryUiState(
    val showTime: String? = null,
    val orderIdAndStartTimeList: List<Map.Entry<Long, Long>>? = null,
)