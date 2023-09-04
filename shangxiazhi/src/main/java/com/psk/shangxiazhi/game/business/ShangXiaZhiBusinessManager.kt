package com.psk.shangxiazhi.game.business

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.twsz.twsystempre.GameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

@OptIn(KoinApiExtension::class)
class ShangXiaZhiBusinessManager(
    lifecycleScope: CoroutineScope,
    deviceManager: DeviceManager,
    deviceName: String,
    deviceAddress: String,
) : BaseBusinessManager<ShangXiaZhi>(lifecycleScope), KoinComponent {
    override val repository = deviceManager.createRepository<ShangXiaZhiRepository>(DeviceType.ShangXiaZhi).apply {
        enable(deviceName, deviceAddress)
        setCallback(onStart = { onStartGame?.invoke() }, onPause = { onPauseGame?.invoke() }, onOver = { onOverGame?.invoke() })
    }

    private val decimalFormat by inject<DecimalFormat>()
    var passiveModule: Boolean = true
    var timeInt: Int = 5
    var speedInt: Int = 20
    var spasmInt: Int = 3
    var resistanceInt: Int = 1
    var intelligent: Boolean = true
    var turn2: Boolean = true
    var onStartGame: (() -> Unit)? = null
    var onPauseGame: (() -> Unit)? = null
    var onOverGame: (() -> Unit)? = null
    private var isStart = AtomicBoolean(false)
    private val report = ShangXiaZhiReport()

    override fun getReport(): IReport {
        return report
    }

    private suspend fun waitStart() {
        while (!isStart.get()) {
            delay(10)
        }
    }

    override suspend fun handleFlow(flow: Flow<ShangXiaZhi>) {
        Log.d(TAG, "startShangXiaZhiJob")
        var isFirstSpasm = false// 是否第一次痉挛
        var mFirstSpasmValue = 0// 第一次痉挛值
        // 这里不能用 distinctUntilChanged、conflate 等操作符，因为需要根据所有数据来计算里程等。必须得到每次数据。
        flow.buffer(Int.MAX_VALUE).collect { shangXiaZhi ->
            report.count++
            val gameData = GameData().apply {
                speed = shangXiaZhi.speedValue
                speedLevel = shangXiaZhi.speedLevel
                spasmLevel = shangXiaZhi.spasmLevel
            }
            // 速度
            report.speedList.add(gameData.speed)
            report.speedTotal += gameData.speed
            report.speedArv = report.speedTotal / report.count
            report.speedMin = if (report.speedMin == -1) {
                gameData.speed
            } else {
                min(report.speedMin, gameData.speed)
            }
            report.speedMax = max(report.speedMax, gameData.speed)
            //模式
            if (shangXiaZhi.model.toInt() == 0x01) {// 被动
                gameData.model = 1// 转换成游戏需要的 0：主动；1：被动
                gameData.resistance = 0
                //被动里程
                report.passiveMil += gameData.speed * 0.5f * 1000 / 3600
                //卡路里
                report.passiveCal += gameData.speed * 0.2f / 300
            } else {// 主动
                gameData.model = 0
                gameData.resistance = shangXiaZhi.res
                //主动里程
                report.activeMil += gameData.speed * 0.5f * 1000 / 3600
                //卡路里
                report.activeCal += gameData.speed * 0.2f * (gameData.resistance * 1.00f / 3.0f) / 60
            }
            gameData.mileage = decimalFormat.format(report.activeMil + report.passiveMil)
            gameData.cal = decimalFormat.format(report.activeCal + report.passiveCal)
            // 阻力
            report.resistanceTotal += gameData.resistance
            report.resistanceArv = report.resistanceTotal / report.count
            report.resistanceMin = if (report.resistanceMin == -1) {
                gameData.resistance
            } else {
                min(report.resistanceMin, gameData.resistance)
            }
            report.resistanceMax = max(report.resistanceMax, gameData.resistance)
            //偏差值：范围0~30 左偏：0~14     十六进制：0x00~0x0e 中：15 	     十六进制：0x0f 右偏：16~30   十六进制：0x10~0x1e
            gameData.offset = shangXiaZhi.offset - 15// 转换成游戏需要的 负数：左；0：不偏移；正数：右；
            // 转换成游戏需要的左边百分比 100~0
            gameData.offsetValue = 100 - shangXiaZhi.offset * 100 / 30
            //痉挛。注意：这里不直接使用 ShangXiaZhi 中的 spasmNum，是因为只要上下肢康复机不关机，那么它返回的痉挛次数值是一直累计的。
            if (shangXiaZhi.spasmNum < 100) {
                if (!isFirstSpasm) {
                    isFirstSpasm = true
                    mFirstSpasmValue = shangXiaZhi.spasmNum
                }
                if (shangXiaZhi.spasmNum - mFirstSpasmValue > report.spasm) {
                    report.spasm = shangXiaZhi.spasmNum - mFirstSpasmValue
                    gameData.spasmFlag = 1
                } else {
                    gameData.spasmFlag = 0
                }
            }
            report.spasmLevelTotal += gameData.spasmLevel
            report.spasmLevelArv = report.spasmLevelTotal / report.count
            report.spasmLevelMin = if (report.spasmLevelMin == -1) {
                gameData.spasmLevel
            } else {
                min(report.spasmLevelMin, gameData.spasmLevel)
            }
            report.spasmLevelMax = max(report.spasmLevelMax, gameData.spasmLevel)
            gameData.spasm = report.spasm
            gameController.updateGameData(gameData)
        }
    }

    override fun onStartGame() {
        super.onStartGame()
        gameController.startGame()
    }

    override fun onPauseGame() {
        super.onPauseGame()
        // 此处不能调用 cancelJob()，因为上下肢需要靠接收数据来判断命令。取消了就收不到数据了。
        gameController.pauseGame()
    }

    override fun onOverGame() {
        super.onOverGame()
        gameController.overGame()
        cancelJob()
    }

    override fun onGameStart() {
        super.onGameStart()
        isStart.compareAndSet(false, true)
    }

    override fun onGameResume() {
        super.onGameResume()
        lifecycleScope.launch(Dispatchers.IO) {
            repository.resume()
        }
    }

    override fun onGamePause() {
        super.onGamePause()
        lifecycleScope.launch(Dispatchers.IO) {
            repository.pause()
        }
    }

    override fun onGameOver() {
        super.onGameOver()
        lifecycleScope.launch(Dispatchers.IO) {
            repository.over()
            cancelJob()
        }
    }

    override fun onGameAppStart() {
        super.onGameAppStart()
        bleManager.connect(DeviceType.ShangXiaZhi, lifecycleScope, 3000L, {
            Log.w(TAG, "上下肢连接成功 $it")
            gameController.updateGameConnectionState(true)
            lifecycleScope.launch(Dispatchers.IO) {
                waitStart()// 等待游戏开始运行后再开始设置数据
                startJob()
                delay(100)
                //设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
                repository.setParams(passiveModule, timeInt, speedInt, spasmInt, resistanceInt, intelligent, turn2)
            }
        }) {
            Log.e(TAG, "上下肢连接失败 $it")
            gameController.updateGameConnectionState(false)
            cancelJob()
        }
    }

    companion object {
        private val TAG = ShangXiaZhiBusinessManager::class.java.simpleName
    }
}