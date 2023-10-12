package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.data.model.IReport
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.RemoteCallback
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
class MultiBusinessManager : RemoteCallback.Stub(), KoinComponent {
    private val gameController by inject<GameController>()
    private val businessManager = mutableMapOf<DeviceType, BaseBusinessManager<*, *>>()
    var onReport: ((List<IReport>) -> Unit)? = null
    private val shangXiaZhiBusinessManager: ShangXiaZhiBusinessManager?
        get() = businessManager.values.firstOrNull { it is ShangXiaZhiBusinessManager } as? ShangXiaZhiBusinessManager

    fun add(deviceType: DeviceType, manager: BaseBusinessManager<*, *>) {
        businessManager[deviceType] = manager
    }

    fun clear() {
        businessManager.clear()
    }

    fun onStartGame() {
        Log.d(TAG, "onStartGame")
        businessManager.values.forEach {
            it.onStartGame()
        }
    }

    fun onPauseGame() {
        Log.d(TAG, "onPauseGame")
        businessManager.values.forEach {
            it.onPauseGame()
        }
    }

    fun onOverGame() {
        Log.d(TAG, "onOverGame")
        businessManager.values.forEach {
            it.onOverGame()
        }
    }

    override fun onGameLoading() {
        Log.i(TAG, "onGameLoading")
        shangXiaZhiBusinessManager?.onGameLoading()
    }

    override fun onGameStart() {
        Log.i(TAG, "onGameStart")
        shangXiaZhiBusinessManager?.onGameStart()
    }

    override fun onGameResume() {
        Log.i(TAG, "onGameResume")
        shangXiaZhiBusinessManager?.onGameResume()
    }

    override fun onGamePause() {
        Log.i(TAG, "onGamePause")
        shangXiaZhiBusinessManager?.onGamePause()
    }

    override fun onGameOver() {
        Log.i(TAG, "onGameOver")
        shangXiaZhiBusinessManager?.onGameOver()
    }

    override fun onGameAppStart() {
        Log.i(TAG, "onGameAppStart")
        businessManager.values.forEach {
            it.onGameAppStart()
        }
    }

    override fun onGameAppFinish() {
        Log.i(TAG, "onGameAppFinish")
        gameController.destroy()
        // 由于 connectExecutor.close() 方法不会触发 connect() 方法的 onDisconnected 回调，原因见 Ble 框架的 close 方法
        // 所以 gameController 中的连接状态相关的方法需要单独调用才能更新界面状态。
        // connectExecutor.close() 必须放到最后面，否则会由于蓝牙的关闭而无法执行某些需要蓝牙的方法。
        businessManager.values.forEach {
            it.onGameAppFinish()
        }
        onReport?.invoke(businessManager.values.map { it.getReport() })
        businessManager.clear()
    }

    companion object {
        private val TAG = MultiBusinessManager::class.java.simpleName

        fun createBusinessManager(
            lifecycleScope: CoroutineScope,
            medicalOrderId: Long,
            repositoryManager: RepositoryManager,
            deviceType: DeviceType,
            deviceName: String,
            deviceAddress: String
        ): BaseBusinessManager<*, *> {
            val className = "${BaseBusinessManager::class.java.`package`?.name}.${deviceType.name}BusinessManager"
            val clazz = Class.forName(className)
            val constructor = clazz.getConstructor(
                CoroutineScope::class.java,
                Long::class.java,
                RepositoryManager::class.java,
                String::class.java,
                String::class.java
            )
            constructor.isAccessible = true
            return constructor.newInstance(
                lifecycleScope,
                medicalOrderId,
                repositoryManager,
                deviceName,
                deviceAddress
            ) as BaseBusinessManager<*, *>
        }
    }
}