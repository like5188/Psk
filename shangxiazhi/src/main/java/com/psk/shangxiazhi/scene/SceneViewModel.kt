package com.psk.shangxiazhi.scene

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.DeviceRepository
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
import org.koin.core.qualifier.named
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class SceneViewModel(
    private val deviceRepository: DeviceRepository,
    private val gameController: GameController
) : ViewModel(), KoinComponent {
    private var fetchShangXiaZhiAndSaveJob: Job? = null

    @OptIn(KoinApiExtension::class)
    private val sdf: SimpleDateFormat by inject(named("yyyyMMdd"))

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
            gameController.init(scene, existHeart)
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
                //设置上下肢参数
                deviceRepository.setShangXiaZhiParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
                delay(100)
                //启动上下肢
                deviceRepository.startShangXiaZhi()
                delay(100)
                //从上下肢获取数据并保存到数据库
                fetchShangXiaZhiAndSave()
            }
        }) {
            Log.e(TAG, "上下肢连接失败")
            gameController.updateGameConnectionState(false)
        }
    }

    fun stopShangXiaZhi() {
        viewModelScope.launch {
            deviceRepository.stopShangXiaZhi()
        }
    }

    fun pauseShangXiaZhi() {
        viewModelScope.launch {
            deviceRepository.pauseShangXiaZhi()
        }
    }

    fun destroy() {
        fetchShangXiaZhiAndSaveJob?.cancel()
        fetchShangXiaZhiAndSaveJob = null
        gameController.destroy()
    }

    private fun getShangXiaZhi(flow: Flow<ShangXiaZhi?>) {
        viewModelScope.launch {
            flow.distinctUntilChanged().conflate().collect { value ->
                Log.v(TAG, "getShangXiaZhi $value")
                value ?: return@collect
                gameController.updateGameData(
                    GameData(
                        model = value.model.toInt(),
                        speed = value.speedValue,
                        speedLevel = value.speedLevel,
                        time = sdf.format(Date()),
                        mileage = "11",
                        cal = DecimalFormat("######0.00").format(10.0),
                        resistance = 10,
                        offset = value.offset,
                        offsetValue = 20,
                        spasm = value.spasmNum,
                        spasmLevel = value.spasmLevel,
                        spasmFlag = 110,
                    )
                )
            }
        }
    }

    private fun fetchShangXiaZhiAndSave() {
        fetchShangXiaZhiAndSaveJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "fetchShangXiaZhiAndSave")
            try {
                deviceRepository.fetchShangXiaZhiAndSave(1)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val TAG = SceneViewModel::class.java.simpleName
    }

}
