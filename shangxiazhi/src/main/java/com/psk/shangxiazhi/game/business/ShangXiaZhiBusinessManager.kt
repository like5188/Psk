package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ShangXiaZhiBusinessManager(
    lifecycleScope: CoroutineScope,
    medicalOrderId: Long,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<ShangXiaZhiRepository>(
    lifecycleScope, medicalOrderId, deviceName, deviceAddress, DeviceType.ShangXiaZhi
) {
    var onStartGame: (() -> Unit)? = null
    var onPauseGame: (() -> Unit)? = null
    var onOverGame: (() -> Unit)? = null
    private var isStart = AtomicBoolean(false)

    init {
        bleDeviceRepository.setCallback(
            onStart = { onStartGame?.invoke() },
            onPause = { onPauseGame?.invoke() },
            onOver = { onOverGame?.invoke() }
        )
    }

    override fun getReport(): IReport? {
        return try {
            ShangXiaZhiReport.report
        } catch (e: UninitializedPropertyAccessException) {
            null
        }
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startShangXiaZhiJob")
        val flow = bleDeviceRepository.getFlow(this, medicalOrderId)
        ShangXiaZhiReport.createForm(flow).collect {
            gameController.updateGameData(it)
        }
    }

    override fun onConnected() {
        Log.w(TAG, "上下肢连接成功")
        gameController.updateGameConnectionState(true)
        lifecycleScope.launch(Dispatchers.IO) {
            waitStart()// 等待游戏开始运行
            startJob()
        }
    }

    override fun onDisconnected() {
        Log.e(TAG, "上下肢连接失败")
        cancelJob()
        gameController.updateGameConnectionState(false)
    }

    override fun onStartGame() {
        gameController.startGame()
    }

    override fun onPauseGame() {
        // 此处不能调用 cancelJob()，因为上下肢需要靠接收数据来判断命令。取消了就收不到数据了。
        gameController.pauseGame()
    }

    override fun onOverGame() {
        cancelJob()
        gameController.overGame()
    }

    fun onGameLoading() {}

    fun onGameStart() {
        isStart.compareAndSet(false, true)
    }

    fun onGameResume() {
        lifecycleScope.launch(Dispatchers.IO) {
            bleDeviceRepository.start()
        }
    }

    fun onGamePause() {
        lifecycleScope.launch(Dispatchers.IO) {
            bleDeviceRepository.pause()
        }
    }

    fun onGameOver() {
        lifecycleScope.launch(Dispatchers.IO) {
            bleDeviceRepository.stop()
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