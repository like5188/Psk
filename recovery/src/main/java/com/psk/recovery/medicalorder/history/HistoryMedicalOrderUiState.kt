package com.psk.recovery.medicalorder.history

import com.like.common.util.mvi.Event
import com.psk.common.util.ToastEvent
import com.psk.recovery.data.model.BloodOxygen
import com.psk.recovery.data.model.BloodPressure
import com.seeker.luckychart.model.ECGPointValue

data class HistoryMedicalOrderUiState(
    val bloodOxygen: BloodOxygen? = null,
    val bloodPressure: BloodPressure? = null,
    val heartRate: Int? = null,
    // 用于医嘱回放时查看某一时间点的心电数据。需要动画
    val ecgPointValue: ECGPointValue? = null,
    // 用于医嘱回放时查看某一时间点的心电数据。不需要动画，一次性显示完1秒钟的所有数据。
    val ecgPointValues: List<ECGPointValue>? = null,
    val progress: Int = 0,
    val maxProgress: Int = 0,
    val curTimeString: String = "",
    val toastEvent: Event<ToastEvent>? = null,
//    val maxBloodOxygen: Int = 0,
//    val minBloodOxygen: Int = 0,
//    val arvBloodOxygen: Int = 0,
//    val maxSBP: Int = 0,
//    val minSBP: Int = 0,
//    val arvSBP: Int = 0,
//    val maxDBP: Int = 0,
//    val minDBP: Int = 0,
//    val arvDBP: Int = 0,
//    val maxHeartRate: Int = 0,
//    val minHeartRate: Int = 0,
//    val arvHeartRate: Int = 0,
)
