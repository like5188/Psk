package com.psk.shangxiazhi.train

import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.data.model.IReport
import com.twsz.twsystempre.TrainScene

data class TrainUiState(
    val toastEvent: Event<ToastEvent>? = null,
    val isTrainCompleted: Boolean = false,
    val deviceMap: Map<DeviceType, BleScanInfo>? = null,
    val healthInfo: HealthInfo? = null,
    val scene: TrainScene? = null,
    val reports: List<IReport>? = null
)