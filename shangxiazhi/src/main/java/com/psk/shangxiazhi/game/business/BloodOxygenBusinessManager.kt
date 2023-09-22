package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.Device
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BloodOxygenBusinessManager(
    lifecycleScope: CoroutineScope,
    medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodOxygen, BloodOxygenRepository>(
    lifecycleScope, medicalOrderId, deviceManager, deviceName, deviceAddress, DeviceType.BloodOxygen
) {

    override fun getReport(): IReport {
        return BloodOxygenReport.report
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startBloodOxygenJob")
        val flow = repository.getFlow(this, medicalOrderId, 1000)
        launch {
            BloodOxygenReport.createForm(flow)
        }
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodOxygenData(value.value)
        }
    }

    override fun onConnected(device: Device) {
        Log.w(TAG, "血氧仪连接成功 $device")
        gameController.updateBloodOxygenConnectionState(true)
        startJob()
    }

    override fun onDisconnected(device: Device) {
        Log.e(TAG, "血氧仪连接失败 $device")
        gameController.updateBloodOxygenConnectionState(false)
        cancelJob()
    }

    companion object {
        private val TAG = BloodOxygenBusinessManager::class.java.simpleName
    }

}
