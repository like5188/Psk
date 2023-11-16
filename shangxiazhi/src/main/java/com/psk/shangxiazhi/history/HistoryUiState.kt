package com.psk.shangxiazhi.history

data class HistoryUiState(
    val showTime: String? = null,
    val medicalIdAndStartTimeList: List<Map.Entry<Long, Long>>? = null,
)