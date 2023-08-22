package com.psk.shangxiazhi.scene

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.BleManager
import com.psk.device.DeviceType
import com.psk.device.Tip
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.DeviceRepository
import com.twsz.twsystempre.GameCallback
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.GameData
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat

@OptIn(KoinApiExtension::class)
class SceneViewModel(
    private val deviceRepository: DeviceRepository, private val gameController: GameController
) : ViewModel(), KoinComponent {
    private var fetchShangXiaZhiAndSaveJob: Job? = null
    private var fetchHeartRateAndSaveJob: Job? = null
    private var fetchBloodOxygenAndSaveJob: Job? = null
    private var fetchBloodPressureAndSaveJob: Job? = null
    private val decimalFormat by inject<DecimalFormat>()
    private val bleManager by inject<BleManager>()

    /**
     * 初始化蓝牙相关的工具类
     * @param onTip     蓝牙相关的提示
     */
    fun initBle(
        activity: ComponentActivity,
        onTip: ((Tip) -> Unit)? = {
            Log.e(TAG, "onTip ${it.msg}")
        }
    ) {
        bleManager.onTip = onTip
        viewModelScope.launch {
            bleManager.init(activity)
        }
    }

    fun start(
        existHeart: Boolean,
        existBloodOxygen: Boolean,
        existBloodPressure: Boolean,
        scene: TrainScene,
        passiveModule: Boolean = true,
        timeInt: Int = 5,
        speedInt: Int = 20,
        spasmInt: Int = 3,
        resistanceInt: Int = 1,
        intelligent: Boolean = true,
        turn2: Boolean = true
    ) {
        val gameCallback = object : GameCallback.Stub() {
            var isStart = false

            private suspend fun waitStart() {
                while (!isStart) {
                    delay(10)
                }
            }

            override fun onLoading() {
                Log.d(TAG, "game onLoading")
                val curTime = System.currentTimeMillis() / 1000
                deviceRepository.enableShangXiaZhi()
                getShangXiaZhi(deviceRepository.listenLatestShangXiaZhi(curTime))
                if (existHeart) {
                    deviceRepository.enableHeartRate()
                    getHeartRate(deviceRepository.listenLatestHeartRate(curTime))
                }
                if (existBloodOxygen) {
                    deviceRepository.enableBloodOxygen()
                    getBloodOxygen(deviceRepository.listenLatestBloodOxygen(curTime))
                }
                if (existBloodPressure) {
                    deviceRepository.enableBloodPressure()
                    getBloodPressure(deviceRepository.listenLatestBloodPressure(curTime))
                }
                bleManager.connectAll(viewModelScope, 3000L, onConnected = {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.w(TAG, "上下肢连接成功 $it")
                            gameController.updateGameConnectionState(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                waitStart()
                                fetchShangXiaZhiAndSave(existHeart, existBloodOxygen, existBloodPressure)
                                delay(100)
                                //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                                deviceRepository.setShangXiaZhiParams(
                                    passiveModule,
                                    timeInt,
                                    speedInt,
                                    spasmInt,
                                    resistanceInt,
                                    intelligent,
                                    turn2
                                )
                            }
                        }

                        DeviceType.HeartRate -> {
                            Log.w(TAG, "心电仪连接成功 $it")
                            gameController.updateEcgConnectionState(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                waitStart()
                                fetchHeartRateAndSave()
                            }
                        }

                        DeviceType.BloodOxygen -> {
                            Log.w(TAG, "血氧仪连接成功 $it")
                            gameController.updateBloodOxygenConnectionState(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                waitStart()
                                fetchBloodOxygenAndSave()
                            }
                        }

                        DeviceType.BloodPressure -> {
                            Log.w(TAG, "血压仪连接成功 $it")
                            gameController.updateBloodPressureConnectionState(true)
                            viewModelScope.launch(Dispatchers.IO) {
                                waitStart()
                                fetchBloodPressureAndSave()
                            }
                        }

                        else -> {}
                    }
                }) {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.e(TAG, "上下肢连接失败 $it")
                            gameController.updateGameConnectionState(false)
                        }

                        DeviceType.HeartRate -> {
                            Log.e(TAG, "心电仪连接失败 $it")
                            gameController.updateEcgConnectionState(false)
                        }

                        DeviceType.BloodOxygen -> {
                            Log.e(TAG, "血氧仪连接失败 $it")
                            gameController.updateBloodOxygenConnectionState(false)
                        }

                        DeviceType.BloodPressure -> {
                            Log.e(TAG, "血压仪连接失败 $it")
                            gameController.updateBloodPressureConnectionState(false)
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
                viewModelScope.launch(Dispatchers.IO) {
                    deviceRepository.resumeShangXiaZhi()
                    startFetchAndSaveJobExceptShangXiaZhi(existHeart, existBloodOxygen, existBloodPressure)
                }
            }

            override fun onPause() {
                Log.d(TAG, "game onPause")
                viewModelScope.launch(Dispatchers.IO) {
                    deviceRepository.pauseShangXiaZhi()
                    cancelFetchAndSaveJobExceptShangXiaZhi()
                }
            }

            override fun onOver() {
                Log.d(TAG, "game onOver")
                viewModelScope.launch(Dispatchers.IO) {
                    deviceRepository.overShangXiaZhi()
                    fetchShangXiaZhiAndSaveJob?.cancel()
                    fetchShangXiaZhiAndSaveJob = null
                    cancelFetchAndSaveJobExceptShangXiaZhi()
                    bleManager.onDestroy()
                }
            }

            override fun onFinish() {
                Log.d(TAG, "game onFinish")
                onCleared()
            }

        }
        viewModelScope.launch(Dispatchers.IO) {
            gameController.initGame(existHeart, existBloodOxygen, existBloodPressure, scene, gameCallback)
        }
    }

    private fun startFetchAndSaveJobExceptShangXiaZhi(
        existHeart: Boolean,
        existBloodOxygen: Boolean,
        existBloodPressure: Boolean,
    ) {
        if (existHeart) {
            fetchHeartRateAndSave()
        }
        if (existBloodOxygen) {
            fetchBloodOxygenAndSave()
        }
        if (existBloodPressure) {
            fetchBloodPressureAndSave()
        }
    }

    private fun cancelFetchAndSaveJobExceptShangXiaZhi() {
        fetchHeartRateAndSaveJob?.cancel()
        fetchHeartRateAndSaveJob = null
        fetchBloodOxygenAndSaveJob?.cancel()
        fetchBloodOxygenAndSaveJob = null
        fetchBloodPressureAndSaveJob?.cancel()
        fetchBloodPressureAndSaveJob = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        gameController.destroy()
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
        cancelFetchAndSaveJobExceptShangXiaZhi()
        bleManager.onDestroy()
    }

    private fun getShangXiaZhi(flow: Flow<ShangXiaZhi?>) {
        viewModelScope.launch(Dispatchers.IO) {
            var mActiveMil = 0f// 主动里程数
            var mPassiveMil = 0f// 被动里程数
            var totalCal = 0f// 总卡路里
            var isFirstSpasm = false// 是否第一次痉挛
            var mFirstSpasmValue = 0// 第一次痉挛值
            var spasm = 0// 痉挛值
            // 这里不能用 distinctUntilChanged、conflate 等操作符，因为需要根据所有数据来计算里程等。必须得到每次数据。
            flow.buffer(Int.MAX_VALUE).collect { shangXiaZhi ->
                shangXiaZhi ?: return@collect
                //转速
                val speed = shangXiaZhi.speedValue
                //阻力
                var resistance = 0
                //模式
                var model = shangXiaZhi.model.toInt()
                if (model == 0x01) {// 被动
                    model = 1// 转换成游戏需要的 0：主动；1：被动
                    resistance = 0
                    //被动里程
                    mPassiveMil += speed * 0.5f * 1000 / 3600
                    //卡路里
                    totalCal += speed * 0.2f / 300
                } else {// 主动
                    model = 0
                    resistance = shangXiaZhi.res
                    //主动里程
                    mActiveMil += speed * 0.5f * 1000 / 3600
                    //卡路里
                    val resParam: Float = resistance * 1.00f / 3.0f
                    totalCal += speed * 0.2f * resParam / 60
                }
                //里程
                val mileage = decimalFormat.format((mActiveMil + mPassiveMil).toDouble())
                //偏差值：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
                val offset = shangXiaZhi.offset - 15// 转换成游戏需要的 负数：左；0：不偏移；正数：右；
                // 转换成游戏需要的左边百分比 100~0
                val offsetValue = 100 - shangXiaZhi.offset * 100 / 30
                //痉挛
                var spasmFlag = 0
                if (shangXiaZhi.spasmNum < 100) {
                    if (!isFirstSpasm) {
                        isFirstSpasm = true
                        mFirstSpasmValue = shangXiaZhi.spasmNum
                    }
                    if (shangXiaZhi.spasmNum - mFirstSpasmValue > spasm) {
                        spasm = shangXiaZhi.spasmNum - mFirstSpasmValue
                        spasmFlag = 1
                    } else {
                        spasmFlag = 0
                    }
                }
                gameController.updateGameData(
                    GameData(
                        model = model,
                        speed = speed,
                        speedLevel = shangXiaZhi.speedLevel,
                        time = "00:00",
                        mileage = mileage,
                        cal = decimalFormat.format(totalCal),
                        resistance = resistance,
                        offset = offset,
                        offsetValue = offsetValue,
                        spasm = spasm,
                        spasmLevel = shangXiaZhi.spasmLevel,
                        spasmFlag = spasmFlag,
                    )
                )
            }
        }
    }

    private fun getHeartRate(flow: Flow<HeartRate?>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                gameController.updateHeartRateData(value)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.coorYValues
            }.buffer(Int.MAX_VALUE).collect { coorYValues ->
                // 注意：此处不能使用 onEach 进行每个数据的延迟，因为只要延迟，由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
                // 这也导致1秒钟时间段内，就算延迟1毫秒，实际上延迟却会达到10多毫秒，导致最多只能发射60多个数据（实际测试）。
                // 这就导致远远达不到心电仪的采样率的100多，从而会导致数据堆积越来越多，导致界面看起来会延迟很严重。
                coorYValues.toList().chunked(5).forEach {
                    // 5个一组，125多的采样率，那么1秒钟发射25组数据就好，平均每个数据需要延迟40毫秒。
                    delay(1)
                    gameController.updateEcgData(it.toFloatArray())
                }
            }
        }
    }

    private fun getBloodOxygen(flow: Flow<BloodOxygen?>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow.distinctUntilChanged().conflate().collect { value ->
                gameController.updateBloodOxygenData(value?.value ?: 0)
            }
        }
    }

    private fun getBloodPressure(flow: Flow<BloodPressure?>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow.distinctUntilChanged().conflate().collect { value ->
                gameController.updateBloodPressureData(value?.sbp ?: 0, value?.dbp ?: 0)
            }
        }
    }

    private fun fetchShangXiaZhiAndSave(
        existHeart: Boolean,
        existBloodOxygen: Boolean,
        existBloodPressure: Boolean,
    ) {
        if (fetchShangXiaZhiAndSaveJob != null) {
            return
        }
        fetchShangXiaZhiAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchShangXiaZhiAndSave")
            try {
                deviceRepository.fetchShangXiaZhiAndSave(1, onStart = {
                    viewModelScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "game onStart by shang xia zhi")
                        gameController.startGame()
                        startFetchAndSaveJobExceptShangXiaZhi(existHeart, existBloodOxygen, existBloodPressure)
                    }
                }, onPause = {
                    viewModelScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "game onPause by shang xia zhi")
                        gameController.pauseGame()
                        cancelFetchAndSaveJobExceptShangXiaZhi()
                    }
                }, onOver = {
                    viewModelScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "game onOver by shang xia zhi")
                        gameController.overGame()
                        fetchShangXiaZhiAndSaveJob?.cancel()
                        fetchShangXiaZhiAndSaveJob = null
                        cancelFetchAndSaveJobExceptShangXiaZhi()
                        bleManager.onDestroy()
                    }
                })
            } catch (e: Exception) {
            }
        }
    }

    private fun fetchHeartRateAndSave() {
        if (fetchHeartRateAndSaveJob != null) {
            return
        }
        fetchHeartRateAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchHeartRateAndSave")
            try {
                deviceRepository.fetchHeartRateAndSave(1)
            } catch (e: Exception) {
            }
        }
    }

    private fun fetchBloodOxygenAndSave() {
        if (fetchBloodOxygenAndSaveJob != null) {
            return
        }
        fetchBloodOxygenAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d(TAG, "fetchBloodOxygenAndSave")
                deviceRepository.fetchBloodOxygenAndSave(1)
                delay(1000)
            }
        }
    }

    private fun fetchBloodPressureAndSave() {
        if (fetchBloodPressureAndSaveJob != null) {
            return
        }
        fetchBloodPressureAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d(TAG, "fetchBloodPressureAndSave")
                deviceRepository.fetchBloodPressureAndSave(1)
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(1000)
            }
        }
    }

    companion object {
        private val TAG = SceneViewModel::class.java.simpleName
    }

}
