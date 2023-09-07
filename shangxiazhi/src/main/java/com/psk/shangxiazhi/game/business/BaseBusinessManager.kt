package com.psk.shangxiazhi.game.business

import com.psk.ble.BleManager
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
    deviceType: DeviceType,
) : KoinComponent {
    protected val bleManager by inject<BleManager>()
    protected val gameController by inject<GameController>()
    protected val repository: Repository = deviceManager.createRepository<Repository>(deviceType).apply {
        enable(deviceName, deviceAddress)
    }
    private var job: Job? = null

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

    protected abstract suspend fun handleFlow(flow: Flow<Data>)
    abstract fun getReport(): IReport

    // 上下肢控制游戏
    open fun onStartGame() {}
    open fun onPauseGame() {}
    open fun onOverGame() {}

    // 游戏控制上下肢
    open fun onGameLoading() {}
    open fun onGameStart() {}
    open fun onGameResume() {}
    open fun onGamePause() {}
    open fun onGameOver() {}
    open fun onGameAppStart() {}
    open fun onGameAppFinish() {}

}
