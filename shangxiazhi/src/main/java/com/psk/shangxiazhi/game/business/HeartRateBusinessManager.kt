package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class HeartRateBusinessManager(
    lifecycleScope: CoroutineScope,
    medicalOrderId: Long,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<HeartRateRepository>(
    lifecycleScope,
    medicalOrderId,
    deviceName,
    deviceAddress,
    DeviceType.HeartRate
) {

    override fun getReport(): IReport? {
        return try {
            HeartRateReport.report
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startHeartRateJob")
        val flow = bleDeviceRepository.getFlow(this, medicalOrderId)
        launch {
            HeartRateReport.createForm(flow)
        }
        launch {
            flow.filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                gameController.updateHeartRateData(value)
            }
        }
        val sampleRate = bleDeviceRepository.getSampleRate()
        val interval = 1000 / sampleRate// 绘制每个数据的间隔时间
        val recommendInterval = 30.0// 建议循环间隔时间
        val drawDataCountEachTime =
            if (interval < recommendInterval) ceil(recommendInterval / interval).toInt() else interval// Math.ceil()向上取整
        val period = 1000L / sampleRate * drawDataCountEachTime// 循环绘制周期间隔
        flow.filterNotNull().map {
            it.coorYValues
        }.buffer(Int.MAX_VALUE).collect { coorYValues ->
            // 取反，因为如果不处理，画出的波形图是反的
//            gameController.updateEcgData(coorYValues.map { -it }.toFloatArray())
            // 注意：此处不能使用 onEach 进行每个数据的延迟，因为只要延迟，由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
            // 这也导致1秒钟时间段内，就算延迟1毫秒，实际上延迟却会达到10多毫秒，导致最多只能发射60多个数据（实际测试）。
            // 这就导致远远达不到心电仪的采样率的100多，从而会导致数据堆积越来越多，导致界面看起来会延迟很严重。
            coorYValues.toList().chunked(drawDataCountEachTime).forEach {
                // 5个一组，125多的采样率，那么1秒钟发射25组数据就好，平均每个数据需要延迟40毫秒。
                delay(period)
                gameController.updateEcgData(it.toFloatArray())
            }
        }
    }

    override fun onConnected() {
        Log.w(TAG, "心电仪连接成功")
        gameController.updateEcgConnectionState(true)
        startJob()
    }

    override fun onDisconnected() {
        Log.e(TAG, "心电仪连接失败")
        lifecycleScope.launch {
            cancelJob()
            gameController.updateEcgConnectionState(false)
        }
    }

    companion object {
        private val TAG = HeartRateBusinessManager::class.java.simpleName
    }
}