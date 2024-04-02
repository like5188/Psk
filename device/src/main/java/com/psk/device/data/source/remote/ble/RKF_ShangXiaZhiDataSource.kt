package com.psk.device.data.source.remote.ble

import com.like.common.util.Logger
import com.psk.device.data.model.Protocol
import com.psk.device.data.model.ShangXiaZhi
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.device.data.source.remote.ble.base.BaseShangXiaZhiDataSource
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

    // 上下肢开始运行或者从暂停中恢复运行时回调
    var onStart: (() -> Unit)? = null

    // 上下肢暂停时回调
    var onPause: (() -> Unit)? = null

    // 上下肢结束时回调
    var onOver: (() -> Unit)? = null

    override fun fetch(orderId: Long): Flow<ShangXiaZhi> = channelFlow {
        shangXiaZhiDataParser.receiver = object : ShangXiaZhiReceiver {
            // 如果是主动模式，那么会一直收到上下肢发来的数据，即使没有运行，也会收到速度为0的数据；
            // 如果是被动模式，那么只有上下肢开始运行时才有数据，没有运行时不会有数据。即不会出现主动模式那种速度为0的数据；
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
                        orderId = orderId,
                    )
                )
            }

            override fun onPause() {
                onPause?.invoke()
            }

            override fun onStop() {
                onOver?.invoke()
            }

        }
        Logger.i("RKF_ShangXiaZhiDataSource setNotifyCallback")
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
