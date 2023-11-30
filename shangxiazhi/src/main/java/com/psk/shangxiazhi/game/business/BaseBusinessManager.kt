package com.psk.shangxiazhi.game.business

import com.like.common.util.Logger
import com.psk.common.CommonApplication
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.BaseBleDeviceRepository
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
       先启动上下肢再启动游戏，由于上下肢设备一般都会比其它比如心电仪先连接上，当上下肢连接上并接收到数据时，会触发onStartGame()回调。
       然后就会触发其它设备的startJob()->setNotifyCallback()方法，但是此时其它设备有可能还没有连接上，所以就会造成setNotifyCallback()失败，
       然后当其它设备连接成功后，又由于已经startJob了，造成job != null，从而不会去重新setNotifyCallback，从而造成了它们虽然连接上了，但是收不到任何数据。
       2023-11-29 16:48:28.350 I/BaseConnectExecutor: 开始连接 00:1B:10:3A:01:2C
       2023-11-29 16:48:28.352 I/BaseConnectExecutor: 开始连接 A0:02:19:00:02:19
       2023-11-29 16:48:28.883 I/BaseConnectExecutor: 连接成功 00:1B:10:3A:01:2C
       2023-11-29 16:48:28.897 W/Logger: 上下肢，setNotifyCallback
       2023-11-29 16:48:29.140 W/Logger: 心电仪，setNotifyCallback
       2023-11-29 16:48:29.181 E/Logger: setNotifyCallback 失败：蓝牙设备未连接:A0:02:19:00:02:19
       2023-11-29 16:48:29.673 I/BaseConnectExecutor: 连接成功 A0:02:19:00:02:19

       所以这里加一个是否连接isConnected()的判断，但是有可能这个方法判断已经连接，
       但是由于ble库中还没有释放锁，造成错误：setNotifyCallback 失败：正在建立连接，请稍后！
       所以这里还需要判断是否持有锁isLocked()。
       2023-11-30 09:41:38.642 I/BaseConnectExecutor: 开始连接 00:1B:10:3A:01:2C
       2023-11-30 09:41:38.642 I/BaseConnectExecutor: 开始连接 A0:02:19:00:02:19
       2023-11-30 09:41:39.564 I/BaseConnectExecutor: 连接成功 00:1B:10:3A:01:2C
       2023-11-30 09:41:39.564 W/ShangXiaZhiBusinessManager: 上下肢连接成功
       2023-11-30 09:41:39.567 D/ShangXiaZhiBusinessManager: startShangXiaZhiJob
       2023-11-30 09:41:39.571 I/Logger: RKF_ShangXiaZhiDataSource setNotifyCallback
       2023-11-30 09:41:39.845 E/Logger: HeartRateBusinessManager isConnected=true
       2023-11-30 09:41:39.849 D/HeartRateBusinessManager: startHeartRateJob
       2023-11-30 09:41:39.863 I/Logger: A0_HeartRateDataSource setNotifyCallback
       2023-11-30 09:41:39.882 W/Logger: setNotifyCallback 失败：正在建立连接，请稍后！
       2023-11-30 09:41:39.968 I/BaseConnectExecutor: 连接成功 A0:02:19:00:02:19
       2023-11-30 09:41:39.969 W/HeartRateBusinessManager: 心电仪连接成功
        */
        Logger.w("${this::class.java.simpleName} isConnected=${bleDeviceRepository.isConnected()} isLocked=${bleDeviceRepository.isLocked()}")
        if (bleDeviceRepository.isConnected() && !bleDeviceRepository.isLocked()) {
            job = lifecycleScope.launch(Dispatchers.IO) {
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
