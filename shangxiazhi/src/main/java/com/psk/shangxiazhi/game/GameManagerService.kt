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
import kotlinx.coroutines.delay
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
                    bloodOxygenManager = BloodOxygenManager(deviceManager).apply {
                        enable(bleScanInfo.name, bleScanInfo.address)
                        onBloodOxygenDataChanged = {
                            gameController.updateBloodOxygenData(it)
                        }
                    }
                }

                DeviceType.BloodPressure -> {
                    bloodPressureManager = BloodPressureManager(deviceManager).apply {
                        enable(bleScanInfo.name, bleScanInfo.address)
                        onBloodPressureDataChanged = { sbp, dbp ->
                            gameController.updateBloodPressureData(sbp, dbp)
                        }
                    }
                }

                DeviceType.HeartRate -> {
                    heartRateManager = HeartRateManager(deviceManager).apply {
                        enable(bleScanInfo.name, bleScanInfo.address)
                        onHeartRateDataChanged = {
                            gameController.updateHeartRateData(it)
                        }
                        onEcgDataChanged = {
                            gameController.updateEcgData(it)
                        }
                    }
                }

                DeviceType.ShangXiaZhi -> {
                    shangXiaZhiManager = ShangXiaZhiManager(deviceManager).apply {
                        enable(bleScanInfo.name, bleScanInfo.address)
                        onStartGame = {
                            Log.d(TAG, "game onStart by shang xia zhi")
                            gameController.startGame()
                            startJobsExceptShangXiaZhi()
                        }
                        onPauseGame = {
                            Log.d(TAG, "game onPause by shang xia zhi")
                            gameController.pauseGame()
                            cancelJobsExceptShangXiaZhi()
                        }
                        onOverGame = {
                            Log.d(TAG, "game onOver by shang xia zhi")
                            gameController.overGame()
                            shangXiaZhiManager?.cancelJob()
                            cancelJobsExceptShangXiaZhi()
                            destroyBle()
                        }
                        onGameDataChanged = {
                            gameController.updateGameData(it)
                        }
                    }
                }
            }
        }

        val gameCallback = object : GameCallback.Stub() {
            var isStart = false

            private suspend fun waitStart() {
                while (!isStart) {
                    delay(10)
                }
            }

            override fun onLoading() {
                Log.d(TAG, "game onLoading")
                bleManager.connectAll(lifecycleScope, 3000L, onConnected = {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.w(TAG, "上下肢连接成功 $it")
                            gameController.updateGameConnectionState(true)
                            lifecycleScope.launch(Dispatchers.IO) {
                                waitStart()
                                shangXiaZhiManager?.startJob(lifecycleScope)
                                delay(100)
                                //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                                shangXiaZhiManager?.repository?.setParams(
                                    passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2
                                )
                            }
                        }

                        DeviceType.HeartRate -> {
                            Log.w(TAG, "心电仪连接成功 $it")
                            gameController.updateEcgConnectionState(true)
                            lifecycleScope.launch(Dispatchers.IO) {
                                waitStart()
                                heartRateManager?.startJob(lifecycleScope)
                            }
                        }

                        DeviceType.BloodOxygen -> {
                            Log.w(TAG, "血氧仪连接成功 $it")
                            gameController.updateBloodOxygenConnectionState(true)
                            lifecycleScope.launch(Dispatchers.IO) {
                                waitStart()
                                bloodOxygenManager?.startJob(lifecycleScope)
                            }
                        }

                        DeviceType.BloodPressure -> {
                            Log.w(TAG, "血压仪连接成功 $it")
                            gameController.updateBloodPressureConnectionState(true)
                            lifecycleScope.launch(Dispatchers.IO) {
                                waitStart()
                                bloodPressureManager?.startJob(lifecycleScope)
                            }
                        }

                        else -> {}
                    }
                }) {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.e(TAG, "上下肢连接失败 $it")
                            gameController.updateGameConnectionState(false)
                            shangXiaZhiManager?.cancelJob()
                        }

                        DeviceType.HeartRate -> {
                            Log.e(TAG, "心电仪连接失败 $it")
                            gameController.updateEcgConnectionState(false)
                            heartRateManager?.cancelJob()
                        }

                        DeviceType.BloodOxygen -> {
                            Log.e(TAG, "血氧仪连接失败 $it")
                            gameController.updateBloodOxygenConnectionState(false)
                            bloodOxygenManager?.cancelJob()
                        }

                        DeviceType.BloodPressure -> {
                            Log.e(TAG, "血压仪连接失败 $it")
                            gameController.updateBloodPressureConnectionState(false)
                            bloodPressureManager?.cancelJob()
                        }

                        else -> {}
                    }
                }
            }

            override fun onStart() {
                isStart = true
                Log.d(TAG, "game onStart")
            }

            override fun onResume() {
                Log.d(TAG, "game onResume")
                lifecycleScope.launch(Dispatchers.IO) {
                    shangXiaZhiManager?.repository?.resume()
                    startJobsExceptShangXiaZhi()
                }
            }

            override fun onPause() {
                Log.d(TAG, "game onPause")
                lifecycleScope.launch(Dispatchers.IO) {
                    shangXiaZhiManager?.repository?.pause()
                    cancelJobsExceptShangXiaZhi()
                }
            }

            override fun onOver() {
                Log.d(TAG, "game onOver")
                lifecycleScope.launch(Dispatchers.IO) {
                    shangXiaZhiManager?.repository?.over()
                    shangXiaZhiManager?.cancelJob()
                    cancelJobsExceptShangXiaZhi()
                    destroyBle()
                }
            }

            override fun onFinish() {
                Log.d(TAG, "game onFinish")
                onDestroy()
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

    private fun startJobsExceptShangXiaZhi() {
        heartRateManager?.startJob(lifecycleScope)
        bloodOxygenManager?.startJob(lifecycleScope)
        bloodPressureManager?.startJob(lifecycleScope)
    }

    private fun cancelJobsExceptShangXiaZhi() {
        heartRateManager?.cancelJob()
        bloodOxygenManager?.cancelJob()
        bloodPressureManager?.cancelJob()
    }

    private fun destroyBle() {
        // 由于 bleManager.onDestroy() 方法不会触发 connect() 方法的 onDisconnected 回调，原因见 Ble 框架的 close 方法
        // 所以只能单独调用 updateXxxConnectionState 方法更新界面状态。
        bleManager.onDestroy()
        if (shangXiaZhiManager != null) {
            gameController.updateGameConnectionState(false)
        }
        if (heartRateManager != null) {
            gameController.updateEcgConnectionState(false)
        }
        if (bloodOxygenManager != null) {
            gameController.updateBloodOxygenConnectionState(false)
        }
        if (bloodPressureManager != null) {
            gameController.updateBloodPressureConnectionState(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        shangXiaZhiManager?.cancelJob()
        cancelJobsExceptShangXiaZhi()
        destroyBle()
        gameController.destroy()
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