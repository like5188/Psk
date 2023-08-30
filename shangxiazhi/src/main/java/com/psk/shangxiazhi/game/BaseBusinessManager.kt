package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.BleManager
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.source.IRepository
import com.twsz.twsystempre.GameController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 设备相关的业务管理基类
 */
@OptIn(KoinApiExtension::class)
abstract class BaseBusinessManager<T>(val lifecycleScope: CoroutineScope) : KoinComponent {
    protected val bleManager by inject<BleManager>()
    protected val gameController by inject<GameController>()
    private var job: Job? = null
    protected abstract val repository: IRepository<T>

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

    // 上下肢控制游戏
    open fun onStartGame() {
        Log.d(TAG, "onStartGame")
    }

    // 上下肢控制游戏
    open fun onPauseGame() {
        Log.d(TAG, "onPauseGame")
    }

    // 上下肢控制游戏
    open fun onOverGame() {
        Log.d(TAG, "onOverGame")
    }

    // 游戏控制上下肢
    open fun onGameLoading() {
        Log.i(TAG, "onGameLoading")
    }

    // 游戏控制上下肢
    open fun onGameStart() {
        Log.i(TAG, "onGameStart")
    }

    // 游戏控制上下肢
    open fun onGameResume() {
        Log.i(TAG, "onGameResume")
    }

    // 游戏控制上下肢
    open fun onGamePause() {
        Log.i(TAG, "onGamePause")
    }

    // 游戏控制上下肢
    open fun onGameOver() {
        Log.i(TAG, "onGameOver")
    }

    // 游戏控制上下肢
    open fun onGameAppStart() {
        Log.i(TAG, "onGameAppStart")
    }

    // 游戏控制上下肢
    open fun onGameAppFinish() {
        Log.i(TAG, "onGameAppFinish")
    }

    companion object {
        private val TAG = BaseBusinessManager::class.java.simpleName

        fun create(
            lifecycleScope: CoroutineScope,
            deviceManager: DeviceManager,
            deviceType: DeviceType,
            deviceName: String,
            deviceAddress: String
        ): BaseBusinessManager<*> {
            val className = "${BaseBusinessManager::class.java.`package`?.name}.${deviceType.name}BusinessManager"
            val clazz = Class.forName(className)
            val constructor = clazz.getConstructor(
                CoroutineScope::class.java, DeviceManager::class.java, String::class.java, String::class.java
            )
            constructor.isAccessible = true
            return constructor.newInstance(lifecycleScope, deviceManager, deviceName, deviceAddress) as BaseBusinessManager<*>
        }
    }
}
