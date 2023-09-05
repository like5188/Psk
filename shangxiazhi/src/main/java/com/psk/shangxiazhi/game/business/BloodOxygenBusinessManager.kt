package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.BloodOxygenRepository
import com.psk.shangxiazhi.data.model.BloodOxygenReport
import com.psk.shangxiazhi.data.model.IReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class BloodOxygenBusinessManager(
    lifecycleScope: CoroutineScope,
    medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodOxygen>(lifecycleScope, medicalOrderId) {
    override val repository = deviceManager.createRepository<BloodOxygenRepository>(DeviceType.BloodOxygen).apply {
        enable(deviceName, deviceAddress)
    }

    private val report: BloodOxygenReport by lazy {
        BloodOxygenReport()
    }

    override fun getReport(): IReport {
        return report
    }

    override suspend fun handleFlow(flow: Flow<BloodOxygen>) {
        Log.d(TAG, "startBloodOxygenJob")
        flow.distinctUntilChanged().conflate().collect { value ->
            report.value = value.value
            gameController.updateBloodOxygenData(value.value)
        }
    }

    override fun onStartGame() {
        super.onStartGame()
        startJob()
    }

    override fun onPauseGame() {
        super.onPauseGame()
        cancelJob()
    }

    override fun onOverGame() {
        super.onOverGame()
        cancelJob()
    }

    override fun onGameResume() {
        super.onGameResume()
        startJob()
    }

    override fun onGamePause() {
        super.onGamePause()
        cancelJob()
    }

    override fun onGameOver() {
        super.onGameOver()
        cancelJob()
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
