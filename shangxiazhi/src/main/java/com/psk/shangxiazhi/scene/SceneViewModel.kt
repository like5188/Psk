package com.psk.shangxiazhi.scene

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.DeviceRepository
import com.twsz.twsystempre.GameCallback
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.GameData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val decimalFormat by inject<DecimalFormat>()

    fun start(
        scene: String? = "",
        existHeart: Boolean = false,
        passiveModule: Boolean = true,
        timeInt: Int = 5,
        speedInt: Int = 20,
        spasmInt: Int = 3,
        resistanceInt: Int = 1,
        intelligent: Boolean = true,
        turn2: Boolean = true
    ) {
        viewModelScope.launch {
            //启动游戏
            gameController.init(scene, existHeart, object : GameCallback.Stub() {
                override fun onStart() {
                    Log.v(TAG, "onStart")
                    viewModelScope.launch {
                        deviceRepository.startShangXiaZhi()
                    }
                }

                override fun onPause() {
                    Log.v(TAG, "onPause")
                    viewModelScope.launch {
                        deviceRepository.pauseShangXiaZhi()
                    }
                }

                override fun onOver() {
                    Log.v(TAG, "onOver")
                    viewModelScope.launch {
                        deviceRepository.stopShangXiaZhi()
                    }
                }

            })
        }
        //监听上下肢数据
        getShangXiaZhi(deviceRepository.listenLatestShangXiaZhi(0))
        //连接上下肢
        deviceRepository.connectShangXiaZhi(onConnected = {
            Log.w(TAG, "上下肢连接成功")
            gameController.updateGameConnectionState(true)
            viewModelScope.launch {
                Log.w(
                    TAG,
                    "设置参数：passiveModule=$passiveModule timeInt=$timeInt speedInt=$speedInt spasmInt=$spasmInt resistanceInt=$resistanceInt intelligent=$intelligent turn2=$turn2"
                )
                //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                deviceRepository.setShangXiaZhiParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
                delay(100)
                //从上下肢获取数据并保存到数据库
                fetchShangXiaZhiAndSave()
            }
        }) {
            Log.e(TAG, "上下肢连接失败")
            gameController.updateGameConnectionState(false)
        }
    }

    fun destroy() {
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
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
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getShangXiaZhi $value")
                value ?: return@collect
                //转速
                val speed = value.speedValue
                //阻力
                var resistance = 0
                //模式
                val model = value.model.toInt()
                if (model == 1) {
                    resistance = 0
                    //被动里程
                    mPassiveMil += speed * 0.5f * 1000 / 3600
                    //卡路里
                    totalCal += speed * 0.2f / 300
                } else {
                    resistance = value.res
                    //主动里程
                    mActiveMil += speed * 0.5f * 1000 / 3600
                    //卡路里
                    val resParam: Float = resistance * 1.00f / 3.0f
                    totalCal += speed * 0.2f * resParam / 60
                }
                //里程
                val mileage = decimalFormat.format((mActiveMil + mPassiveMil).toDouble())
                //偏差值
                var offset = value.offset
                val offsetValue = if (offset == 0) {
                    50
                } else if (offset < 0) {
                    if (offset < -15) {
                        offset = -15
                    }
                    100 - Math.abs(offset) * 100 / 15
                } else {
                    if (offset > 15) {
                        offset = 15
                    }
                    Math.abs(offset) * 100 / 15
                }
                //痉挛
                var spasmFlag = 0
                if (value.spasmNum < 100) {
                    if (!isFirstSpasm) {
                        isFirstSpasm = true
                        mFirstSpasmValue = value.spasmNum
                    }
                    if (value.spasmNum - mFirstSpasmValue > spasm) {
                        spasm = value.spasmNum - mFirstSpasmValue
                        spasmFlag = 1
                    } else {
                        spasmFlag = 0
                    }
                }
                gameController.updateGameData(
                    GameData(
                        model = model,
                        speed = speed,
                        speedLevel = value.speedLevel,
                        time = "00:00",
                        mileage = mileage,
                        cal = decimalFormat.format(totalCal),
                        resistance = resistance,
                        offset = offset,
                        offsetValue = offsetValue,
                        spasm = spasm,
                        spasmLevel = value.spasmLevel,
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

    companion object {
        private val TAG = SceneViewModel::class.java.simpleName
    }

}
