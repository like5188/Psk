package com.psk.sixminutes

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event

data class DevicesUiState(
    val date: String = "",
    val sbpBefore: Int = 0,
    val dbpBefore: Int = 0,
    val sbpAfter: Int = 0,
    val dbpAfter: Int = 0,
    val heartRateStatus: String = "未连接",
    val heartRate: Int = 0,
    val ecgDatas: List<List<Float>>? = null,
    val bloodOxygenStatus: String = "未连接",
    val bloodOxygen: Int = 0,
    val toastEvent: Event<ToastEvent>? = null,
)