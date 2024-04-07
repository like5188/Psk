package com.psk.sixminutes

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event
import com.psk.sixminutes.data.model.HealthInfo

data class DevicesUiState(
    val date: String = "",
    val time: String = "--:--",
    val progress: Int = 0,
    val completed: Boolean = false,
    val healthInfo: HealthInfo = HealthInfo(),
    val heartRateStatus: String = "未连接",
    val heartRate: String = "--",
    val ecgDatas: List<List<Float>>? = null,
    val bloodOxygenStatus: String = "未连接",
    val bloodOxygen: String = "--",
    val toastEvent: Event<ToastEvent>? = null,
)