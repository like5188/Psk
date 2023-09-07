package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
    var passiveModule: Boolean = true
    var timeInt: Int = 5
    var speedInt: Int = 20
    var spasmInt: Int = 3
    var resistanceInt: Int = 1
    var intelligent: Boolean = true
    var turn2: Boolean = true
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

    private suspend fun waitStart() {
        while (!isStart.get()) {
            delay(10)
        }
    }

    override suspend fun handleFlow(flow: Flow<ShangXiaZhi>) {
        Log.d(TAG, "startShangXiaZhiJob")
        ShangXiaZhiReport.createForm(flow).collect {
            gameController.updateGameData(it)
        }
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

    override fun onGameAppStart() {
        super.onGameAppStart()
        bleManager.connect(DeviceType.ShangXiaZhi, lifecycleScope, 3000L, {
            Log.w(TAG, "上下肢连接成功 $it")
            gameController.updateGameConnectionState(true)
            lifecycleScope.launch(Dispatchers.IO) {
                waitStart()// 等待游戏开始运行后再开始设置数据
                startJob()
                delay(100)
                //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                repository.setParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
            }
        }) {
            Log.e(TAG, "上下肢连接失败 $it")
            gameController.updateGameConnectionState(false)
            cancelJob()
        }
    }

    companion object {
        private val TAG = ShangXiaZhiBusinessManager::class.java.simpleName
    }
}