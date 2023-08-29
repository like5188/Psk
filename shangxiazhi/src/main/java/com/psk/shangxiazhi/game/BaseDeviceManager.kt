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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(KoinApiExtension::class)
abstract class BaseDeviceManager<T>(val lifecycleScope: CoroutineScope) : KoinComponent {
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
    }

    open fun onPauseGame() {
        Log.d(TAG, "onPauseGame")
    }

    open fun onOverGame() {
        Log.d(TAG, "onOverGame")
    }

    open fun onGameLoading() {
        Log.i(TAG, "onGameLoading")
    }

    open fun onGameStart() {
        Log.i(TAG, "onGameStart")
        isStart.compareAndSet(false, true)
    }

    open fun onGameResume() {
        Log.i(TAG, "onGameResume")
    }

    open fun onGamePause() {
        Log.i(TAG, "onGamePause")
    }

    open fun onGameOver() {
        Log.i(TAG, "onGameOver")
    }

    open fun onGameFinish() {
        Log.i(TAG, "onGameFinish")
    }

    companion object {
        private val TAG = BaseDeviceManager::class.java.simpleName

        fun create(
            lifecycleScope: CoroutineScope,
            deviceManager: DeviceManager,
            deviceType: DeviceType,
            deviceName: String,
            deviceAddress: String
        ): BaseDeviceManager<*> {
            val className = "${BaseDeviceManager::class.java.`package`?.name}.${deviceType.name}Manager"
            val clazz = Class.forName(className)
            val constructor = clazz.getConstructor(
                CoroutineScope::class.java, DeviceManager::class.java, String::class.java, String::class.java
            )
            constructor.isAccessible = true
            return constructor.newInstance(lifecycleScope, deviceManager, deviceName, deviceAddress) as BaseDeviceManager<*>
        }
    }
}
