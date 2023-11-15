package com.psk.device.data.source.remote

import com.psk.device.data.model.Protocol
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.remote.base.BaseShangXiaZhiDataSource
import com.psk.device.util.ShangXiaZhiDataParser
import com.psk.device.util.ShangXiaZhiReceiver
import com.twsz.remotecommands.RemoteCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * 瑞甲上下肢康复机数据源
 */
class RKF_ShangXiaZhiDataSource : BaseShangXiaZhiDataSource() {
    override val protocol = Protocol(
        "0000ffe1-0000-1000-8000-00805f9b34fb",
        "0000ffe2-0000-1000-8000-00805f9b34fb",
        "0000ffe3-0000-1000-8000-00805f9b34fb",
    )
    private val shangXiaZhiDataParser by lazy {
        ShangXiaZhiDataParser()
    }

    // 上下肢开始运行或者从暂停中恢复运行时回调
    var onStart: (() -> Unit)? = null

    // 上下肢暂停时回调
    var onPause: (() -> Unit)? = null

    // 上下肢结束时回调
    var onOver: (() -> Unit)? = null

    private var job: Job? = null

    override fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi> = channelFlow {
        shangXiaZhiDataParser.receiver = object : ShangXiaZhiReceiver {
            override fun onReceive(
                model: Byte,
                speedLevel: Int,
                speed: Int,
                offset: Int,
                spasm: Int,
                spasmLevel: Int,
                resistance: Int,
                intelligence: Byte,
                direction: Byte
            ) {
                onStart?.invoke()
                trySend(
                    ShangXiaZhi(
                        model = model,
                        speedLevel = speedLevel,
                        speed = speed,
                        offset = offset,
                        spasm = spasm,
                        spasmLevel = spasmLevel,
                        resistance = resistance,
                        intelligence = intelligence,
                        direction = direction,
                        medicalOrderId = medicalOrderId,
                    )
                )
                // 如果是主动模式，那么会一直收到上下肢发来的数据，即使没有运行，也会收到速度为0的数据；
                // 如果是被动模式，那么只有上下肢开始运行时才有数据，没有运行时不会有数据。即不会出现主动模式那种速度为0的数据；
                // 所以主动模式切换被动模式需要单独处理。因为上下肢没有数据发送过来。
                if (model == 0x02.toByte()) {
                    // 主动模式
                    job?.cancel()
                    job = this@channelFlow.launch {
                        // 因为上下肢发来的数据间隔大概是1秒，所以这里延迟2秒如果没有收到数据，就认为由主动模式变为了被动模式，并且还没有开始运行。
                        delay(2000)
                        if (isConnected()) {
                            // 模拟发送假数据。被动模式、速度0、时间0
                            trySend(
                                ShangXiaZhi(
                                    model = 0x01.toByte(),
                                    speedLevel = 4,
                                    speed = 0,
                                    offset = 15,
                                    spasmLevel = 6,
                                    resistance = 1,
                                    intelligence = 0x41.toByte(),
                                    direction = 0x51.toByte(),
                                    medicalOrderId = medicalOrderId,
                                )
                            )
                        }
                    }
                }
            }

            override fun onPause() {
                onPause?.invoke()
            }

            override fun onStop() {
                onOver?.invoke()
            }

        }
        setNotifyCallback().collect {
            shangXiaZhiDataParser.putData(it)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun start(): Boolean {
        return write(RemoteCommand.generateStartParam())
    }

    override suspend fun pause(): Boolean {
        return write(RemoteCommand.generatePauseParam())
    }

    override suspend fun stop(): Boolean {
        return write(RemoteCommand.generateStopParam())
    }

    override suspend fun setParams(params: ShangXiaZhiParams): Boolean {
        return write(RemoteCommand.generateParam(params.toTrunkCommandData()))
    }

}
