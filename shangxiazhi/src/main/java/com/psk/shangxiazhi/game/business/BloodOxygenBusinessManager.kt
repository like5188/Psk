package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    lifecycleScope,
    medicalOrderId,
    deviceManager,
    deviceName,
    deviceAddress,
    DeviceType.BloodOxygen
) {

    override fun getReport(): IReport {
        return BloodOxygenReport.report
    }

    override suspend fun handleFlow(flow: Flow<BloodOxygen>) = withContext(Dispatchers.IO) {
        Log.d(TAG, "startBloodOxygenJob")
        launch {
            BloodOxygenReport.createForm(flow)
        }
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodOxygenData(value.value)
        }
    }

    override fun onGameAppStart() {
        super.onGameAppStart()
        bleManager.connect(DeviceType.BloodOxygen, lifecycleScope, 3000L, {
            Log.w(TAG, "血氧仪连接成功 $it")
            gameController.updateBloodOxygenConnectionState(true)
            startJob()
        }) {
            Log.e(TAG, "血氧仪连接失败 $it")
            gameController.updateBloodOxygenConnectionState(false)
            cancelJob()
        }
    }

    companion object {
        private val TAG = BloodOxygenBusinessManager::class.java.simpleName
    }

}
