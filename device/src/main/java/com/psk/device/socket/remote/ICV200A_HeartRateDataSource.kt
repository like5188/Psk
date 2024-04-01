package com.psk.device.socket.remote

import com.like.common.util.Logger
import com.psk.device.data.model.HeartRate
import com.psk.device.socket.remote.base.BaseSocketHeartRateDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * iCV200A心电图仪数据源
 * 模拟数据时每秒收到25次回调，每次回调包含12导心电数据为240 byte，所以某个导联的数据量为 240/12/2(2个字节一个数据)=10，所以采样率为25*10=250
 */
class ICV200A_HeartRateDataSource : BaseSocketHeartRateDataSource() {

    override fun fetch(orderId: Long): Flow<HeartRate> = callbackFlow {
        setOnMessageCallback { message ->
            message ?: return@setOnMessageCallback
            // iCV200A心电图仪模拟数据时每秒收到25次回调，每次回调包含12导心电数据为240 byte，所以某个导联的数据量为 240/12/2(2个字节一个数据)=10，所以采样率为25*10=250
            Logger.v("数据量 ${message.capacity()}, ")
            Logger.v("包头 ${message.short}, ")
            Logger.v("设备编号 ${message.int}, ")
            Logger.v("采样率 ${message.short}, ")
            Logger.v("包采样周期数 ${message.get()}, ")
            Logger.v("导联标识 ${message.get()}, ")
            Logger.v("包序号(时间) ${message.int}, ")
            Logger.v("增益(float) ${message.float}, ")
            val ecgData = (0 until 120).map {
                message.short / -1000f
            }.toFloatArray()
            Logger.v("12导心电数据 ${ecgData}, ")
            val heartRate = message.short.toInt()
            Logger.v("心率 $heartRate, ")
            Logger.v("收缩压 ${message.short}, ")
            Logger.v("舒张压 ${message.short}, ")
            Logger.v("功率 ${message.short}, ")
            Logger.v("血氧 ${message.get()}, ")

            trySend(HeartRate(value = heartRate, coorYValues = ecgData, orderId = orderId))
        }
        awaitClose {
            setOnMessageCallback(null)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSampleRate(): Int {
        return 250
    }

}
