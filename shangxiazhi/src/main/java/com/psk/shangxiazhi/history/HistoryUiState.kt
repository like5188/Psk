package com.psk.shangxiazhi.history

data class HistoryUiState(
    val showTime: String? = null,
    val dateAndDataList: List<DateAndData>? = null,
)

data class DateAndData(
    val time: Long? = null,
    val data: Long? = null,
)