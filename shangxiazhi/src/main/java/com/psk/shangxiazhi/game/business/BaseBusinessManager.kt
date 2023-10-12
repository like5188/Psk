package com.psk.shangxiazhi.game.business

import com.psk.common.CommonApplication
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BaseBleDeviceRepository
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
abstract class BaseBusinessManager<Repository : BaseBleDeviceRepository<*>>(
    protected val lifecycleScope: CoroutineScope,
    protected val medicalOrderId: Long,
    deviceName: String,
    deviceAddress: String,
    deviceType: DeviceType,
) : KoinComponent {
    private var job: Job? = null
    protected val gameController by inject<GameController>()
    protected val bleDeviceRepository = RepositoryManager.createBleDeviceRepository<Repository>(deviceType).apply {
        enable(CommonApplication.Companion.sInstance, deviceName, deviceAddress)
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
        bleDeviceRepository.connect(lifecycleScope, ::onConnected, ::onDisconnected)
    }

    fun onGameAppFinish() {
        bleDeviceRepository.close()
    }

    abstract fun getReport(): IReport
    protected abstract suspend fun run()
    protected abstract fun onConnected()
    protected abstract fun onDisconnected()
}
