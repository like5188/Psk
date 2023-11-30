package com.psk.device.data.source.remote

import com.like.common.util.Logger
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.Protocol
import com.psk.device.data.source.remote.base.BaseHeartRateDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tt.sk.CbeltParsableFactory
import tt.sk.model.CCmd
import tt.sk.model.EcgPack
import tt.sk.model.Pack

/*
1、蓝牙接收的二进制数据解析过程：byte[] -> Pack -> EcgPack

Step1:
Pack.Builder builder = new Pack.Builder()	// 数据包构建者
builder.append(bytes)	// 添加二进制数据
builder.size() > 0		// 表示组包成功，至少有一个完整数据包
builder.iterator()		// 返回数据包列表 Iterator<Pack>

Step2:
CbeltParsableFactory factory = new CbeltParsableFactory() // 数据包解析工厂
// 将数据包解析成相应的实体类对象
Parsable parsable = factory.create(pack, deviceName)
if (parsable instanceof EcgPack) {	// 或者 pack.cmd == CCmd.ECG
  EcgPack ecgPack = (EcgPack) parsable
}
// 其他数据解析过程均同上
// 注意：需要配合相应的心电图显示视图，才能显示。
*/
/**
 * SCI411C 心电仪数据源
 */
class C0_HeartRateDataSource : BaseHeartRateDataSource() {
    override val protocol = Protocol(
        "00000001-0000-1000-8000-00805f9b34fb",
        "00000003-0000-1000-8000-00805f9b34fb",
        "00000002-0000-1000-8000-00805f9b34fb",
    )

    override fun fetch(orderId: Long): Flow<HeartRate> = channelFlow<HeartRate> {
        // 把二进制数据构建成数据包
        val packBuilder = Pack.Builder()

        Logger.i("C0_HeartRateDataSource setNotifyCallback")
        setNotifyCallback().filter {
            packBuilder.append(it)// 添加收到的二进制数据
            packBuilder.size() > 0// 表示组包成功，至少有一个完整数据包
        }
            .flatMapConcat {
                val packs = packBuilder.iterator()// 构建出的多个数据包
                packs.asFlow().onEach {
                    packs.remove()
                }
            }.filter {
                it.cmd == CCmd.ECG
            }
            .map {
                CbeltParsableFactory.create(it, address)// 将数据包解析成相应的实体类对象
            }
            .filterNotNull()
            .buffer()// 因为通知数据很重要，所以不能丢弃，就缓存起来。
            .collect {
                when (it) {
                    is EcgPack -> {
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

    override fun getSampleRate(): Int {
        return 125
    }
}
