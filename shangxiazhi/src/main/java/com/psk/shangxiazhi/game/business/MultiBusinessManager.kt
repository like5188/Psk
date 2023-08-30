package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import kotlinx.coroutines.CoroutineScope

class MultiBusinessManager {
    private val managers = mutableMapOf<DeviceType, BaseBusinessManager<*>>()

    fun add(deviceType: DeviceType, manager: BaseBusinessManager<*>) {
        managers[deviceType] = manager
    }

    fun clear() {
        managers.clear()
    }

    fun contains(deviceType: DeviceType): Boolean {
        return managers.containsKey(deviceType)
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