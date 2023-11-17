package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.data.model.DeviceType
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
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodOxygenRepository>(
    lifecycleScope, medicalOrderId, deviceName, deviceAddress, DeviceType.BloodOxygen
) {

    override fun getReport(): IReport? {
        return try {
            BloodOxygenReport.report
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startBloodOxygenJob")
        val flow = bleDeviceRepository.getFlow(this, medicalOrderId, 1000)
        launch {
            BloodOxygenReport.createForm(flow)
        }
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodOxygenData(value.value)
        }
    }

    override fun onConnected() {
        Log.w(TAG, "血氧仪连接成功")
        gameController.updateBloodOxygenConnectionState(true)
        startJob()
    }

    override fun onDisconnected() {
        Log.e(TAG, "血氧仪连接失败")
        lifecycleScope.launch {
            cancelJob()
            gameController.updateBloodOxygenConnectionState(false)
        }
    }

    companion object {
        private val TAG = BloodOxygenBusinessManager::class.java.simpleName
    }

}
