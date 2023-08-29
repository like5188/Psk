package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.HeartRateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HeartRateManager(
    lifecycleScope: CoroutineScope,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseDeviceManager<HeartRate>(lifecycleScope) {
    override val repository = deviceManager.createRepository<HeartRateRepository>(DeviceType.HeartRate).apply {
        enable(deviceName, deviceAddress)
    }

    override suspend fun handleFlow(flow: Flow<HeartRate>) = withContext<Unit>(Dispatchers.IO) {
        Log.d(TAG, "startHeartRateJob")
        launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                gameController.updateHeartRateData(value)
            }
        }
        launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.coorYValues
            }.buffer(Int.MAX_VALUE).collect { coorYValues ->
                // 注意：此处不能使用 onEach 进行每个数据的延迟，因为只要延迟，由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
                // 这也导致1秒钟时间段内，就算延迟1毫秒，实际上延迟却会达到10多毫秒，导致最多只能发射60多个数据（实际测试）。
                // 这就导致远远达不到心电仪的采样率的100多，从而会导致数据堆积越来越多，导致界面看起来会延迟很严重。
                coorYValues.toList().chunked(5).forEach {
                    // 5个一组，125多的采样率，那么1秒钟发射25组数据就好，平均每个数据需要延迟40毫秒。
                    delay(1)
                    gameController.updateEcgData(it.toFloatArray())
                }
            }
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
        gameController.updateEcgConnectionState(false)
    }

    override fun onGameLoading() {
        super.onGameLoading()
        bleManager.connect(DeviceType.HeartRate, lifecycleScope, 3000L, {
            Log.w(TAG, "心电仪连接成功 $it")
            gameController.updateEcgConnectionState(true)
            lifecycleScope.launch(Dispatchers.IO) {
                waitStart()
                startJob()
            }
        }) {
            Log.e(TAG, "心电仪连接失败 $it")
            gameController.updateEcgConnectionState(false)
            cancelJob()
        }
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
        gameController.updateEcgConnectionState(false)
    }

    override fun onGameFinish() {
        super.onGameFinish()
        cancelJob()
        gameController.updateEcgConnectionState(false)
    }

    companion object {
        private val TAG = HeartRateManager::class.java.simpleName
    }
}