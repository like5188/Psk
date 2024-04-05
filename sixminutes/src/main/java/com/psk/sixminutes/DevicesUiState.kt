package com.psk.sixminutes

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event

data class DevicesUiState(
    val date: String = "",
    val time: String = "--:--",
    val progress: Int = 0,
    val finish: Boolean = false,
    val sbpBefore: String = "--",
    val dbpBefore: String = "--",
    val sbpAfter: String = "--",
    val dbpAfter: String = "--",
    val heartRateStatus: String = "未连接",
    val heartRate: String = "--",
    val ecgDatas: List<List<Float>>? = null,
    val bloodOxygenStatus: String = "未连接",
    val bloodOxygen: String = "--",
    val lapStatus: String = "未连接",
    val lapMeters: String = "--",
    val lapCount: String = "--",
    val toastEvent: Event<ToastEvent>? = null,
)