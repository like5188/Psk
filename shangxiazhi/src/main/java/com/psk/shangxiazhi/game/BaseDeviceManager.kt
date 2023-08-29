package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.BleManager
import com.psk.device.DeviceManager
import com.psk.device.data.source.IRepository
import com.twsz.twsystempre.GameController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(KoinApiExtension::class)
abstract class BaseDeviceManager<T>(
    val lifecycleScope: CoroutineScope,
    val deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : KoinComponent {
    protected val bleManager by inject<BleManager>()
    protected val gameController by inject<GameController>()
    private var job: Job? = null
    protected abstract val repository: IRepository<T>
    private var isStart = AtomicBoolean(false)

    protected suspend fun waitStart() {
        while (!isStart.get()) {
            delay(10)
        }
    }

    fun startJob() {
        if (job != null) {
            return
        }
        job = lifecycleScope.launch(Dispatchers.IO) {
            handleFlow(repository.getFlow(this, 1))
        }
    }

    fun cancelJob() {
        job?.cancel()
        job = null
    }

    protected abstract suspend fun handleFlow(flow: Flow<T>)
    open fun onStartGame() {
        Log.d(TAG, "onStartGame")
        gameController.startGame()
    }

    open fun onPauseGame() {
        Log.d(TAG, "onPauseGame")
        gameController.pauseGame()
    }

    open fun onOverGame() {
        Log.d(TAG, "onOverGame")
        gameController.overGame()
        // 由于 bleManager.onDestroy() 方法不会触发 connect() 方法的 onDisconnected 回调，原因见 Ble 框架的 close 方法
        // 所以只能单独调用 updateXxxConnectionState 方法更新界面状态。
        bleManager.onDestroy()
    }

    open fun onGameLoading() {
        Log.i(TAG, "onGameLoading")
        isStart.compareAndSet(false, true)
    }

    open fun onGameStart() {
        Log.i(TAG, "onGameStart")
    }

    open fun onGameResume() {
        Log.i(TAG, "onGameResume")
    }

    open fun onGamePause() {
        Log.i(TAG, "onGamePause")
    }

    open fun onGameOver() {
        Log.i(TAG, "onGameOver")
        bleManager.onDestroy()
    }

    open fun onGameFinish() {
        Log.i(TAG, "onGameFinish")
        gameController.destroy()
        bleManager.onDestroy()
    }

    companion object {
        private val TAG = BaseDeviceManager::class.java.simpleName
    }
}
