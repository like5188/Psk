package com.psk.device.data.source.remote

import com.like.common.util.SecondsTimer
import com.psk.device.data.model.Protocol
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.remote.base.BaseShangXiaZhiDataSource
import com.psk.device.util.ShangXiaZhiDataParser
import com.psk.device.util.ShangXiaZhiReceiver
import com.twsz.remotecommands.RemoteCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

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

    // 计时器，由于上下肢没有返回时间数据，所以只能自己计算。
    private val secondsTimer by lazy {
        SecondsTimer()
    }

    // 上下肢开始运行或者从暂停中恢复运行时回调
    var onStart: (() -> Unit)? = null

    // 上下肢暂停时回调
    var onPause: (() -> Unit)? = null

    // 上下肢结束时回调
    var onOver: (() -> Unit)? = null

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
                val shangXiaZhi = ShangXiaZhi(
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
                    time = secondsTimer.getSeconds().toInt()
                )
                secondsTimer.startOrResume()
                onStart?.invoke()
                trySend(shangXiaZhi)
            }

            override fun onPause() {
                secondsTimer.pause()
                onPause?.invoke()
            }

            override fun onStop() {
                secondsTimer.stop()
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
