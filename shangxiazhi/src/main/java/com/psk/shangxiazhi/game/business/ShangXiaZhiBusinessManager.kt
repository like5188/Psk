package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.Device
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ShangXiaZhiBusinessManager(
    lifecycleScope: CoroutineScope,
    medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<ShangXiaZhi, ShangXiaZhiRepository>(
    lifecycleScope, medicalOrderId, deviceManager, deviceName, deviceAddress, DeviceType.ShangXiaZhi
) {
    var onStartGame: (() -> Unit)? = null
    var onPauseGame: (() -> Unit)? = null
    var onOverGame: (() -> Unit)? = null
    private var isStart = AtomicBoolean(false)

    init {
        repository.setCallback(onStart = { onStartGame?.invoke() }, onPause = { onPauseGame?.invoke() }, onOver = { onOverGame?.invoke() })
    }

    override fun getReport(): IReport {
        return ShangXiaZhiReport.report
    }

    override suspend fun run() {
        Log.d(TAG, "startShangXiaZhiJob")
        val flow = repository.getFlow(lifecycleScope, medicalOrderId)
        ShangXiaZhiReport.createForm(flow).collect {
            gameController.updateGameData(it)
        }
    }

    override fun onConnected(device: Device) {
        Log.w(TAG, "上下肢连接成功 $device")
        gameController.updateGameConnectionState(true)
        lifecycleScope.launch(Dispatchers.IO) {
            waitStart()// 等待游戏开始运行
            startJob()
        }
    }

    override fun onDisconnected(device: Device) {
        Log.e(TAG, "上下肢连接失败 $device")
        gameController.updateGameConnectionState(false)
        cancelJob()
    }

    override fun onStartGame() {
        gameController.startGame()
    }

    override fun onPauseGame() {
        // 此处不能调用 cancelJob()，因为上下肢需要靠接收数据来判断命令。取消了就收不到数据了。
        gameController.pauseGame()
    }

    override fun onOverGame() {
        gameController.overGame()
        cancelJob()
    }

    override fun onGameStart() {
        isStart.compareAndSet(false, true)
    }

    override fun onGameResume() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.resume()
        }
    }

    override fun onGamePause() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.pause()
        }
    }

    override fun onGameOver() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.over()
            cancelJob()
        }
    }

    private suspend fun waitStart() {
        while (!isStart.get()) {
            delay(10)
        }
    }

    companion object {
        private val TAG = ShangXiaZhiBusinessManager::class.java.simpleName
    }
}