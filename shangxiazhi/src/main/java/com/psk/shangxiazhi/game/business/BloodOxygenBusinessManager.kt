package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.RepositoryManager
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
    repositoryManager: RepositoryManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodOxygenRepository>(
    lifecycleScope, medicalOrderId, repositoryManager, deviceName, deviceAddress, DeviceType.BloodOxygen
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

    override fun onConnected() {
        Log.w(TAG, "血氧仪连接成功")
        gameController.updateBloodOxygenConnectionState(true)
        startJob()
    }

    override fun onDisconnected() {
        Log.e(TAG, "血氧仪连接失败")
        gameController.updateBloodOxygenConnectionState(false)
        cancelJob()
    }

    companion object {
        private val TAG = BloodOxygenBusinessManager::class.java.simpleName
    }

}
