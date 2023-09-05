package com.psk.shangxiazhi.history

data class HistoryUiState(
    val showTime: String? = null,
)

data class DateAndData(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null,
    val data: Long? = null,
)