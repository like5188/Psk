package com.psk.shangxiazhi.game.business

import com.like.common.util.Logger
import com.psk.common.CommonApplication
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BaseBleDeviceRepository
import com.psk.shangxiazhi.data.model.IReport
import com.twsz.twsystempre.GameController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 设备相关的业务管理基类
 */
@OptIn(KoinApiExtension::class)
abstract class BaseBusinessManager<Repository : BaseBleDeviceRepository<*>>(
    protected val lifecycleScope: CoroutineScope,
    protected val orderId: Long,
    deviceName: String,
    deviceAddress: String,
    deviceType: DeviceType,
) : KoinComponent {
    private var job: Job? = null
    protected val gameController by inject<GameController>()
    protected val bleDeviceRepository = DeviceRepositoryManager.createBleDeviceRepository<Repository>(deviceType).apply {
        init(CommonApplication.Companion.sInstance, deviceName, deviceAddress)
    }

    fun startJob() {
        if (job != null) {
            return
        }
        /*
       先启动上下肢再启动游戏，由于上下肢设备一般都会比其它比如心电仪先连接上，当上下肢连接上并接收到数据时，会触发onStartGame()回调，
       然后就会触发其它设备的startJob()->setNotifyCallback()方法，但是此时其它设备有可能还没有连接上，它们setNotifyCallback()就会失败。
       当后面其它设备连接成功后，又由于已经startJob了，造成job != null，从而不会去重新setNotifyCallback，
       造成了它们虽然连接上了，但是收不到任何数据。所以这里加一个是否连接isConnected()的判断。
       2023-11-29 16:48:28.350 I/BaseConnectExecutor: 开始连接 00:1B:10:3A:01:2C
       2023-11-29 16:48:28.352 I/BaseConnectExecutor: 开始连接 A0:02:19:00:02:19
       2023-11-29 16:48:28.883 I/BaseConnectExecutor: 连接成功 00:1B:10:3A:01:2C
       2023-11-29 16:48:28.897 W/Logger: 上下肢，setNotifyCallback
       2023-11-29 16:48:29.140 W/Logger: 心电仪，setNotifyCallback
       2023-11-29 16:48:29.181 E/Logger: setNotifyCallback 失败：蓝牙设备未连接:A0:02:19:00:02:19
       2023-11-29 16:48:29.673 I/BaseConnectExecutor: 连接成功 A0:02:19:00:02:19
        */
        Logger.w("${this::class.java.simpleName} startJob isConnected=${bleDeviceRepository.isConnected()}")
        if (bleDeviceRepository.isConnected()) {
            job = lifecycleScope.launch(Dispatchers.IO) {
                delay(100)// 延迟一下，避免刚连接成功就发送蓝牙命令，此时有可能失败。
                run()
            }
        } else {
            Logger.e("${this::class.java.simpleName} 启动任务失败")
        }
    }

    suspend fun cancelJob() = withContext(Dispatchers.IO) {
        // 这里必须延迟，原因有2点：
        // 1、使最后一条数据成功插入数据库，并触发listenLatest()更新游戏界面数据。
        // 2、有可能由于overGame()方法被先调用，导致游戏界面已经结束，这时就无法更新游戏界面数据了。
        delay(200)
        job?.cancel()
        job = null
    }

    // 上下肢控制游戏
    open fun onStartGame() {
        startJob()
    }

    open fun onPauseGame() {
        lifecycleScope.launch {
            cancelJob()
        }
    }

    open fun onOverGame() {
        lifecycleScope.launch {
            cancelJob()
        }
    }

    // 游戏app启动回调
    fun onGameAppStart() {
        bleDeviceRepository.connect(lifecycleScope, onConnected = ::onConnected, onDisconnected = ::onDisconnected)
    }

    fun onGameAppFinish() {
        bleDeviceRepository.close()
    }

    abstract fun getReport(): IReport?
    protected abstract suspend fun run()
    protected abstract fun onConnected()
    protected abstract fun onDisconnected()
}
