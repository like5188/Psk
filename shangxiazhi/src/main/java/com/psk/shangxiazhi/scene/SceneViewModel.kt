package com.psk.shangxiazhi.scene

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.common.util.asFlow
import com.psk.device.DeviceType
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
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
    private val decimalFormat by inject<DecimalFormat>()

    fun start(
        scene: TrainScene,
        existHeart: Boolean = false,
        passiveModule: Boolean = true,
        timeInt: Int = 5,
        speedInt: Int = 20,
        spasmInt: Int = 3,
        resistanceInt: Int = 1,
        intelligent: Boolean = true,
        turn2: Boolean = true
    ) {
        val gameCallback = object : GameCallback.Stub() {
            override fun onLoading() {
                Log.d(TAG, "onLoading")
                deviceRepository.enableShangXiaZhi()
                if (existHeart) {
                    deviceRepository.enableHeartRate()
                }
                deviceRepository.connectAll(onConnected = {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.w(TAG, "上下肢连接成功")
                            gameController.updateGameConnectionState(true)
                        }

                        DeviceType.HeartRate -> {
                            Log.w(TAG, "心电仪连接成功")
                            gameController.updateEcgConnectionState(true)
                        }

                        else -> {}
                    }
                }) {
                    when (it.type) {
                        DeviceType.ShangXiaZhi -> {
                            Log.e(TAG, "上下肢连接失败")
                            gameController.updateGameConnectionState(false)
                        }

                        DeviceType.HeartRate -> {
                            Log.e(TAG, "心电仪连接失败")
                            gameController.updateEcgConnectionState(false)
                        }

                        else -> {}
                    }
                }
            }

            override fun onStart() {
                Log.d(TAG, "onStart")
                viewModelScope.launch {
                    while (!deviceRepository.isAllDeviceConnected()) {
                        delay(200)
                    }
                    //监听上下肢数据
                    val curTime = System.currentTimeMillis() / 1000
                    getShangXiaZhi(deviceRepository.listenLatestShangXiaZhi(curTime))
                    if (existHeart) {
                        getHeartRate(deviceRepository.listenLatestHeartRate(curTime))
                    }
                    delay(100)
                    startFetchAndSaveJob()
                    delay(100)
                    //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                    deviceRepository.setShangXiaZhiParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
                }
            }

            override fun onResume() {
                Log.d(TAG, "onResume")
                startFetchAndSaveJob()
                viewModelScope.launch {
                    deviceRepository.resumeShangXiaZhi()
                }
            }

            override fun onPause() {
                Log.d(TAG, "onPause")
                cancelFetchAndSaveJob()
                viewModelScope.launch {
                    deviceRepository.pauseShangXiaZhi()
                }
            }

            override fun onOver() {
                Log.d(TAG, "onOver")
                cancelFetchAndSaveJob()
                viewModelScope.launch {
                    deviceRepository.overShangXiaZhi()
                }
            }

        }
        viewModelScope.launch {
            gameController.initGame(scene, existHeart, gameCallback)
        }
    }

    private fun startFetchAndSaveJob() {
        fetchShangXiaZhiAndSave()
        fetchHeartRateAndSave()
    }

    private fun cancelFetchAndSaveJob() {
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
        fetchHeartRateAndSaveJob?.cancel()
        fetchHeartRateAndSaveJob = null
    }

    fun destroy() {
        cancelFetchAndSaveJob()
        gameController.destroy()
    }

    private fun getShangXiaZhi(flow: Flow<ShangXiaZhi?>) {
        viewModelScope.launch {
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

    private fun fetchShangXiaZhiAndSave() {
        fetchShangXiaZhiAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchShangXiaZhiAndSave")
            try {
                deviceRepository.fetchShangXiaZhiAndSave(1, onStart = {
                    gameController.startGame()
                }, onPause = {
                    gameController.pauseGame()
                }, onOver = {
                    gameController.overGame()
                })
            } catch (e: Exception) {
            }
        }
    }

    private fun getHeartRate(flow: Flow<HeartRate?>) {
        viewModelScope.launch {
            flow.filterNotNull().flatMapConcat {
                it.values.asFlow()
            }.distinctUntilChanged().collect { value ->
                gameController.updateHeartRateData(value)
            }
        }
        viewModelScope.launch {
            var count = 0
            (0..3).asFlow()
            flow.filterNotNull().flatMapConcat {
                count = it.coorYValues.size
                it.coorYValues.asFlow()
            }.buffer(count).onEach {
                // count 为心电数据采样率，即 1 秒钟采集 count 次数据。
                delay(1000L / count)
            }.collect { value ->
                gameController.updateEcgData(value)
            }
        }
    }

    private fun fetchHeartRateAndSave() {
        fetchHeartRateAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchHeartRateAndSave")
            try {
                deviceRepository.fetchHeartRateAndSave(1)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val TAG = SceneViewModel::class.java.simpleName
    }

}
