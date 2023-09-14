package com.psk.shangxiazhi.train

import com.like.common.util.mvi.Event
import com.psk.ble.DeviceType
import com.psk.common.util.ToastEvent
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.game.GameManagerService
import com.twsz.twsystempre.TrainScene

data class TrainUiState(
    val toastEvent: Event<ToastEvent>? = null,
    val gameManagerService: GameManagerService? = null,
    val deviceMap: Map<DeviceType, BleScanInfo>? = null,
    val healthInfo: HealthInfo? = null,
    val scene: TrainScene? = null,
    val reports: List<IReport>? = null
)