package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BloodPressureRepository
import com.psk.shangxiazhi.data.model.BloodPressureReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BloodPressureBusinessManager(
    lifecycleScope: CoroutineScope,
    orderId: Long,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodPressureRepository>(
    lifecycleScope, orderId, deviceName, deviceAddress, DeviceType.BloodPressure
) {
    var bloodPressureMeasureType: Int = 0

    override fun getReport(): IReport? {
        return try {
            BloodPressureReport.report
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startBloodPressureJob")
        val flow = when (bloodPressureMeasureType) {
            0 -> bleDeviceRepository.getFetchFlow(this, orderId, 1000)
            1 -> bleDeviceRepository.getMeasureFlow(this, orderId, 1000 * 60 * 5)
            else -> null
        } ?: return@withContext
        launch {
            BloodPressureReport.createForm(flow)
        }
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodPressureData(value.sbp, value.dbp)
        }
    }

    override fun onConnected() {
        Log.w(TAG, "血压仪连接成功")
        gameController.updateBloodPressureConnectionState(true)
        startJob()
    }

    override fun onDisconnected() {
        Log.e(TAG, "血压仪连接失败")
        lifecycleScope.launch {
            cancelJob()
            gameController.updateBloodPressureConnectionState(false)
        }
    }

    companion object {
        private val TAG = BloodPressureBusinessManager::class.java.simpleName
    }
}