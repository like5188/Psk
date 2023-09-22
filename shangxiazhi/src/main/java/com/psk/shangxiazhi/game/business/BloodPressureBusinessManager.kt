package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.Device
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodPressure
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
    medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodPressure, BloodPressureRepository>(
    lifecycleScope, medicalOrderId, deviceManager, deviceName, deviceAddress, DeviceType.BloodPressure
) {
    var bloodPressureMeasureType: Int = 0

    override fun getReport(): IReport {
        return BloodPressureReport.report
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startBloodPressureJob")
        val flow = when (bloodPressureMeasureType) {
            0 -> repository.getFetchFlow(lifecycleScope, medicalOrderId, 1000)
            1 -> repository.getMeasureFlow(lifecycleScope, medicalOrderId, 1000 * 60 * 5)
            else -> null
        } ?: return@withContext
        launch {
            BloodPressureReport.createForm(flow)
        }
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodPressureData(value.sbp, value.dbp)
        }
    }

    override fun onConnected(device: Device) {
        Log.w(TAG, "血压仪连接成功 $device")
        gameController.updateBloodPressureConnectionState(true)
        startJob()
    }

    override fun onDisconnected(device: Device) {
        Log.e(TAG, "血压仪连接失败 $device")
        gameController.updateBloodPressureConnectionState(false)
        cancelJob()
    }

    companion object {
        private val TAG = BloodPressureBusinessManager::class.java.simpleName
    }
}