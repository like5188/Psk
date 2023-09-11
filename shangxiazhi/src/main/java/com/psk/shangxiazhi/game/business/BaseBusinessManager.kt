package com.psk.shangxiazhi.game.business

import com.psk.ble.BleManager
import com.psk.ble.Device
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.source.IRepository
import com.psk.shangxiazhi.data.model.IReport
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
abstract class BaseBusinessManager<Data, Repository : IRepository<Data>>(
    val lifecycleScope: CoroutineScope,
    private val medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
    private val deviceType: DeviceType,
) : KoinComponent {
    private var job: Job? = null
    private val bleManager by inject<BleManager>()
    protected val gameController by inject<GameController>()
    protected val repository: Repository by lazy {
        deviceManager.createRepository<Repository>(deviceType).apply {
            enable(deviceName, deviceAddress)
        }
    }

    fun startJob() {
        if (job != null) {
            return
        }
        job = lifecycleScope.launch(Dispatchers.IO) {
            handleFlow(repository.getFlow(this, medicalOrderId))
        }
    }

    fun cancelJob() {
        job?.cancel()
        job = null
    }

    // 上下肢控制游戏
    open fun onStartGame() {
        startJob()
    }

    open fun onPauseGame() {
        cancelJob()
    }

    open fun onOverGame() {
        cancelJob()
    }

    // 游戏控制上下肢
    open fun onGameLoading() {}
    open fun onGameStart() {}

    open fun onGameResume() {
        startJob()
    }

    open fun onGamePause() {
        cancelJob()
    }

    open fun onGameOver() {
        cancelJob()
    }

    open fun onGameAppStart() {
        bleManager.connect(deviceType, lifecycleScope, 3000L, ::onConnected, ::onDisconnected)
    }

    open fun onGameAppFinish() {}

    abstract fun getReport(): IReport
    protected abstract suspend fun handleFlow(flow: Flow<Data>)
    protected abstract fun onConnected(device: Device)
    protected abstract fun onDisconnected(device: Device)
}
