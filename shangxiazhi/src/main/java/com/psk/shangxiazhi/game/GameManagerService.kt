package com.psk.shangxiazhi.game

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.psk.ble.BleManager
import com.psk.ble.DeviceType
import com.psk.ble.Tip
import com.psk.device.DeviceManager
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.game.business.MultiBusinessManager
import com.psk.shangxiazhi.game.business.ShangXiaZhiBusinessManager
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.RemoteCallback
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 游戏管理服务，如果需要和游戏app进行交互，就使用此服务。
 */
@OptIn(KoinApiExtension::class)
class GameManagerService : Service(), KoinComponent {
    companion object {
        private val TAG = GameManagerService::class.java.simpleName
    }

    private val bleManager by inject<BleManager>()
    private val deviceManager by inject<DeviceManager>()
    private val gameController by inject<GameController>()
    private lateinit var lifecycleScope: CoroutineScope
    private val multiBusinessManager: MultiBusinessManager by lazy {
        MultiBusinessManager()
    }

    /**
     * 初始化蓝牙相关的工具类
     * @param onTip     蓝牙相关的提示
     */
    fun initBle(activity: ComponentActivity, onTip: ((Tip) -> Unit)? = { Log.e(TAG, "onTip ${it.msg}") }) {
        lifecycleScope = activity.lifecycleScope
        bleManager.onTip = onTip
        lifecycleScope.launch {
            deviceManager.init()
            bleManager.init(activity)
        }
    }

    fun start(
        devices: Map<DeviceType, BleScanInfo>,
        scene: TrainScene,
        passiveModule: Boolean = true,
        timeInt: Int = 5,
        speedInt: Int = 20,
        spasmInt: Int = 3,
        resistanceInt: Int = 1,
        intelligent: Boolean = true,
        turn2: Boolean = true
    ) {
        val existShangXiaZhi = devices.containsKey(DeviceType.ShangXiaZhi)
        val existHeart = devices.containsKey(DeviceType.HeartRate)
        val existBloodOxygen = devices.containsKey(DeviceType.BloodOxygen)
        val existBloodPressure = devices.containsKey(DeviceType.BloodPressure)
        if (!existShangXiaZhi && !existHeart && !existBloodOxygen && !existBloodPressure) {
            return
        }
        multiBusinessManager.clear()
        devices.forEach {
            val deviceType = it.key
            val bleScanInfo = it.value
            MultiBusinessManager.createBusinessManager(
                lifecycleScope, deviceManager, deviceType, bleScanInfo.name, bleScanInfo.address
            ).apply {
                multiBusinessManager.add(deviceType, this)
                if (this is ShangXiaZhiBusinessManager) {
                    this.passiveModule = passiveModule
                    this.timeInt = timeInt
                    this.speedInt = speedInt
                    this.spasmInt = spasmInt
                    this.resistanceInt = resistanceInt
                    this.intelligent = intelligent
                    this.turn2 = turn2
                    this.onStartGame = {
                        multiBusinessManager.onStartGame()
                    }
                    this.onPauseGame = {
                        multiBusinessManager.onPauseGame()
                    }
                    this.onOverGame = {
                        multiBusinessManager.onOverGame()
                    }
                }
            }
        }
        val remoteCallback = object : RemoteCallback.Stub() {
            override fun onGameLoading() {
                multiBusinessManager.onGameLoading()
            }

            override fun onGameStart() {
                multiBusinessManager.onGameStart()
            }

            override fun onGameResume() {
                multiBusinessManager.onGameResume()
            }

            override fun onGamePause() {
                multiBusinessManager.onGamePause()
            }

            override fun onGameOver() {
                multiBusinessManager.onGameOver()
            }

            override fun onGameAppStart() {
                multiBusinessManager.onGameAppStart()
            }

            override fun onGameAppFinish() {
                multiBusinessManager.onGameAppFinish()
            }

        }
        lifecycleScope.launch(Dispatchers.IO) {
            // todo 如果增加蓝牙设备系列，需要在这里结合游戏app做处理。
            gameController.initGame(
                existShangXiaZhi,
                existHeart,
                existBloodOxygen,
                existBloodPressure,
                scene,
                remoteCallback
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        multiBusinessManager.onGameAppFinish()
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    /**
     * 用于本地同进程调用
     */
    inner class LocalBinder : Binder() {
        fun getService(): GameManagerService {
            return this@GameManagerService
        }
    }

}