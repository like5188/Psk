package com.psk.shangxiazhi.game.business

import com.psk.device.DeviceManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.IBleDeviceRepository
import com.psk.shangxiazhi.data.model.IReport
import com.twsz.twsystempre.GameController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 设备相关的业务管理基类
 */
@OptIn(KoinApiExtension::class)
abstract class BaseBusinessManager<Data, Repository : IBleDeviceRepository<Data>>(
    protected val lifecycleScope: CoroutineScope,
    protected val medicalOrderId: Long,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
    deviceType: DeviceType,
) : KoinComponent {
    private var job: Job? = null
    protected val gameController by inject<GameController>()
    protected val repository: Repository = deviceManager.createRepository<Repository>(deviceType).apply {
        enable(deviceName, deviceAddress)
    }

    fun startJob() {
        if (job != null) {
            return
        }
        job = lifecycleScope.launch(Dispatchers.IO) {
            run()
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

    // 游戏app启动回调
    fun onGameAppStart() {
        repository.connect(lifecycleScope, ::onConnected, ::onDisconnected)
    }

    fun onGameAppFinish() {
        repository.close()
    }

    abstract fun getReport(): IReport
    protected abstract suspend fun run()
    protected abstract fun onConnected()
    protected abstract fun onDisconnected()
}
