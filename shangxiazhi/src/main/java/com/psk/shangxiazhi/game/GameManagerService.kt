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
import com.twsz.twsystempre.GameCallback
import com.twsz.twsystempre.GameController
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
    private var bloodOxygenManager: BloodOxygenManager? = null
    private var bloodPressureManager: BloodPressureManager? = null
    private var heartRateManager: HeartRateManager? = null
    private var shangXiaZhiManager: ShangXiaZhiManager? = null
    private lateinit var lifecycleScope: CoroutineScope

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
        devices.forEach {
            val deviceType = it.key
            val bleScanInfo = it.value
            when (deviceType) {
                DeviceType.BloodOxygen -> {
                    bloodOxygenManager = BloodOxygenManager(lifecycleScope, deviceManager, bleScanInfo.name, bleScanInfo.address)
                }

                DeviceType.BloodPressure -> {
                    bloodPressureManager = BloodPressureManager(lifecycleScope, deviceManager, bleScanInfo.name, bleScanInfo.address)
                }

                DeviceType.HeartRate -> {
                    heartRateManager = HeartRateManager(lifecycleScope, deviceManager, bleScanInfo.name, bleScanInfo.address)
                }

                DeviceType.ShangXiaZhi -> {
                    shangXiaZhiManager = ShangXiaZhiManager(
                        passiveModule,
                        timeInt,
                        speedInt,
                        spasmInt,
                        resistanceInt,
                        intelligent,
                        turn2,
                        lifecycleScope,
                        deviceManager,
                        bleScanInfo.name,
                        bleScanInfo.address
                    ).apply {
                        onStartGame = {
                            bloodOxygenManager?.onStartGame()
                            bloodPressureManager?.onStartGame()
                            heartRateManager?.onStartGame()
                            shangXiaZhiManager?.onStartGame()
                        }
                        onPauseGame = {
                            bloodOxygenManager?.onPauseGame()
                            bloodPressureManager?.onPauseGame()
                            heartRateManager?.onPauseGame()
                            shangXiaZhiManager?.onPauseGame()
                        }
                        onOverGame = {
                            bloodOxygenManager?.onOverGame()
                            bloodPressureManager?.onOverGame()
                            heartRateManager?.onOverGame()
                            shangXiaZhiManager?.onOverGame()
                        }
                    }
                }
            }
        }

        val gameCallback = object : GameCallback.Stub() {
            override fun onLoading() {
                bloodOxygenManager?.onGameLoading()
                bloodPressureManager?.onGameLoading()
                heartRateManager?.onGameLoading()
                shangXiaZhiManager?.onGameLoading()
            }

            override fun onStart() {
                bloodOxygenManager?.onGameStart()
                bloodPressureManager?.onGameStart()
                heartRateManager?.onGameStart()
                shangXiaZhiManager?.onGameStart()
            }

            override fun onResume() {
                bloodOxygenManager?.onGameResume()
                bloodPressureManager?.onGameResume()
                heartRateManager?.onGameResume()
                shangXiaZhiManager?.onGameResume()
            }

            override fun onPause() {
                bloodOxygenManager?.onGamePause()
                bloodPressureManager?.onGamePause()
                heartRateManager?.onGamePause()
                shangXiaZhiManager?.onGamePause()
            }

            override fun onOver() {
                bloodOxygenManager?.onGameOver()
                bloodPressureManager?.onGameOver()
                heartRateManager?.onGameOver()
                shangXiaZhiManager?.onGameOver()
            }

            override fun onFinish() {
                bloodOxygenManager?.onGameFinish()
                bloodPressureManager?.onGameFinish()
                heartRateManager?.onGameFinish()
                shangXiaZhiManager?.onGameFinish()
            }

        }
        lifecycleScope.launch(Dispatchers.IO) {
            gameController.initGame(
                heartRateManager != null,
                bloodOxygenManager != null,
                bloodPressureManager != null,
                scene, gameCallback
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        bloodOxygenManager?.onGameFinish()
        bloodPressureManager?.onGameFinish()
        heartRateManager?.onGameFinish()
        shangXiaZhiManager?.onGameFinish()
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