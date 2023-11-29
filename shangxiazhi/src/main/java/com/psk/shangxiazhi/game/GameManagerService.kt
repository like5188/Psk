package com.psk.shangxiazhi.game

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.like.common.util.startForeground
import com.like.common.util.stopForeground
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.game.business.BloodPressureBusinessManager
import com.psk.shangxiazhi.game.business.MultiBusinessManager
import com.psk.shangxiazhi.game.business.ShangXiaZhiBusinessManager
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * 游戏管理服务，如果需要和游戏app进行交互，就使用此服务。
 */
class GameManagerService : Service() {
    companion object {
        private val TAG = GameManagerService::class.java.simpleName
    }

    private val gameController by inject<GameController>()
    private val multiBusinessManager: MultiBusinessManager by lazy {
        MultiBusinessManager()
    }

    fun start(
        scope: CoroutineScope,
        orderId: Long,
        devices: Map<DeviceType, BleScanInfo>,
        scene: TrainScene,
        bloodPressureMeasureType: Int,
        onReport: ((List<IReport>) -> Unit)? = null
    ) {
        multiBusinessManager.clear()
        multiBusinessManager.onReport = {
            onReport?.invoke(it)
        }
        devices.forEach {
            val deviceType = it.key
            val bleScanInfo = it.value
            MultiBusinessManager.createBusinessManager(
                scope, orderId, deviceType, bleScanInfo.name, bleScanInfo.address
            ).apply {
                multiBusinessManager.add(deviceType, this)
                when (this) {
                    is ShangXiaZhiBusinessManager -> {
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

                    is BloodPressureBusinessManager -> {
                        this.bloodPressureMeasureType = bloodPressureMeasureType
                    }
                }
            }
        }
        scope.launch(Dispatchers.IO) {
            // todo 如果增加蓝牙设备系列，需要在这里结合游戏app做处理。
            gameController.initGame(
                devices.containsKey(DeviceType.HeartRate),
                devices.containsKey(DeviceType.BloodOxygen),
                devices.containsKey(DeviceType.BloodPressure),
                scene,
                multiBusinessManager
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "GameManagerService onCreate")
        startForeground("游戏服务", "游戏服务正在运行", R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "GameManagerService onDestroy")
        stopForeground()
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