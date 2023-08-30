package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.BloodPressureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class BloodPressureBusinessManager(
    lifecycleScope: CoroutineScope,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<BloodPressure>(lifecycleScope) {
    override val repository = deviceManager.createRepository<BloodPressureRepository>(DeviceType.BloodPressure).apply {
        enable(deviceName, deviceAddress)
    }

    override suspend fun handleFlow(flow: Flow<BloodPressure>) {
        Log.d(TAG, "startBloodPressureJob")
        flow.distinctUntilChanged().conflate().collect { value ->
            gameController.updateBloodPressureData(value.sbp, value.dbp)
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
        gameController.updateBloodPressureConnectionState(false)
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
        gameController.updateBloodPressureConnectionState(false)
    }

    override fun onGameAppStart() {
        super.onGameAppStart()
        bleManager.connect(DeviceType.BloodPressure, lifecycleScope, 3000L, {
            Log.w(TAG, "血压仪连接成功 $it")
            gameController.updateBloodPressureConnectionState(true)
            startJob()
        }) {
            Log.e(TAG, "血压仪连接失败 $it")
            gameController.updateBloodPressureConnectionState(false)
            cancelJob()
        }
    }

    companion object {
        private val TAG = BloodPressureBusinessManager::class.java.simpleName
    }
}