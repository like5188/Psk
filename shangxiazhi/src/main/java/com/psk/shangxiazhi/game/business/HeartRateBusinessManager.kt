package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import com.psk.shangxiazhi.data.model.HeartRateReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        gameController.setEcgConfig(bleDeviceRepository.getSampleRate())
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
        flow.filterNotNull().map {
            it.coorYValues
        }.buffer(Int.MAX_VALUE).collect { coorYValues ->
            // 取反，因为如果不处理，画出的波形图是反的
            gameController.updateEcgData(coorYValues.map { -it }.toFloatArray())
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