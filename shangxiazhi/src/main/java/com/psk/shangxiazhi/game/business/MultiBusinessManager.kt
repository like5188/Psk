package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.BleManager
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.twsz.twsystempre.GameController
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
class MultiBusinessManager : KoinComponent {
    private val bleManager by inject<BleManager>()
    private val gameController by inject<GameController>()
    private val managers = mutableMapOf<DeviceType, BaseBusinessManager<*>>()

    fun add(deviceType: DeviceType, manager: BaseBusinessManager<*>) {
        managers[deviceType] = manager
    }

    fun clear() {
        managers.clear()
    }

    fun onStartGame() {
        Log.d(TAG, "onStartGame")
        managers.values.forEach {
            it.onStartGame()
        }
    }

    fun onPauseGame() {
        Log.d(TAG, "onPauseGame")
        managers.values.forEach {
            it.onPauseGame()
        }
    }

    fun onOverGame() {
        Log.d(TAG, "onOverGame")
        managers.values.forEach {
            it.onOverGame()
        }
    }

    fun onGameLoading() {
        Log.i(TAG, "onGameLoading")
        managers.values.forEach {
            it.onGameLoading()
        }
    }

    fun onGameStart() {
        Log.i(TAG, "onGameStart")
        managers.values.forEach {
            it.onGameStart()
        }
    }

    fun onGameResume() {
        Log.i(TAG, "onGameResume")
        managers.values.forEach {
            it.onGameResume()
        }
    }

    fun onGamePause() {
        Log.i(TAG, "onGamePause")
        managers.values.forEach {
            it.onGamePause()
        }
    }

    fun onGameOver() {
        Log.i(TAG, "onGameOver")
        managers.values.forEach {
            it.onGameOver()
        }
    }

    fun onGameAppStart() {
        Log.i(TAG, "onGameAppStart")
        managers.values.forEach {
            it.onGameAppStart()
        }
    }

    fun onGameAppFinish() {
        Log.i(TAG, "onGameAppFinish")
        managers.values.forEach {
            it.onGameAppFinish()
        }
        gameController.destroy()
        // 由于 bleManager.onDestroy() 方法不会触发 connect() 方法的 onDisconnected 回调，原因见 Ble 框架的 close 方法
        // 所以 gameController 中的连接状态相关的方法需要单独调用才能更新界面状态。
        // bleManager.onDestroy() 必须放到最后面，否则会由于蓝牙的关闭而无法执行某些需要蓝牙的方法。
        bleManager.onDestroy()
    }

    companion object {
        private val TAG = MultiBusinessManager::class.java.simpleName

        fun createBusinessManager(
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