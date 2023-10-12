package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.ShangXiaZhi
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
    repositoryManager: RepositoryManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<ShangXiaZhi, ShangXiaZhiRepository>(
    lifecycleScope, medicalOrderId, repositoryManager, deviceName, deviceAddress, DeviceType.ShangXiaZhi
) {
    var onStartGame: (() -> Unit)? = null
    var onPauseGame: (() -> Unit)? = null
    var onOverGame: (() -> Unit)? = null
    private var isStart = AtomicBoolean(false)

    init {
        repository.setCallback(
            onStart = { onStartGame?.invoke() },
            onPause = { onPauseGame?.invoke() },
            onOver = { onOverGame?.invoke() }
        )
    }

    override fun getReport(): IReport {
        return ShangXiaZhiReport.report
    }

    override suspend fun run() = withContext(Dispatchers.IO) {
        Log.d(TAG, "startShangXiaZhiJob")
        val flow = repository.getFlow(this, medicalOrderId)
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

    fun onGameLoading() {}

    fun onGameStart() {
        isStart.compareAndSet(false, true)
    }

    fun onGameResume() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.resume()
        }
    }

    fun onGamePause() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.pause()
        }
    }

    fun onGameOver() {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.over()
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