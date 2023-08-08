package com.psk.recovery.data.source.remote.ble

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.Protocol
import com.psk.recovery.data.model.ShangXiaZhi
import com.psk.recovery.data.source.remote.IShangXiaZhiDataSource
import com.starcaretech.stardata.StarData
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.Arrays

class XZX_ShangXiaZhiDataSource(
    private val bleManager: BleManager
) : IShangXiaZhiDataSource {
    private val protocol = Protocol(
        "0000ffe1-0000-1000-8000-00805f9b34fb",
        "0000ffe2-0000-1000-8000-00805f9b34fb",
        "0000ffe3-0000-1000-8000-00805f9b34fb",
    )
    private val device = Device("00:1B:10:3A:01:2C", protocol)

    override fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)?) {
        bleManager.addDevices(device)
        bleManager.connect(true, onConnected = {
            StarData.init()
            onConnected()
        }) {
            onDisconnected?.invoke()
        }
    }

    override suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi> = channelFlow {
        val buffer: ByteBuf = Unpooled.buffer(1024 * 1000)
        val bufferShort = Unpooled.buffer(1024 * 1000)
        val FRAME_LENGTH = 13
        val FRAME_LENGTH_SHORT = 5
        bleManager.setNotifyCallback(device)?.collect {
            if (it.isEmpty()) return@collect
            // 解析游戏数据
            try {
                buffer.writeBytes(it)
                while (buffer.readableBytes() >= FRAME_LENGTH) {
                    val bufTemp: ByteBuf = buffer.readBytes(1)
                    val bytesTemp = ByteArray(1)
                    bufTemp.readBytes(bytesTemp)
                    if (bytesTemp[0] == 0xA3.toByte()) {
                        buffer.markReaderIndex()
                        val bufTemp1: ByteBuf = buffer.readBytes(3)
                        val bytesTemp1 = ByteArray(3)
                        bufTemp1.readBytes(bytesTemp1)
                        if (bytesTemp1[0] == 0x21.toByte() && bytesTemp1[1] == 0x20.toByte() && bytesTemp1[2] == 0x80.toByte()) {
                            val bufTemp2: ByteBuf = buffer.readBytes(FRAME_LENGTH - 4)
                            val bytesTemp2 = ByteArray(FRAME_LENGTH - 4)
                            bufTemp2.readBytes(bytesTemp2)

                            //重新组帧
                            val bytesTemp3 = ByteArray(FRAME_LENGTH)
                            bytesTemp3[0] = 0xA3.toByte()
                            bytesTemp3[1] = 0x21.toByte()
                            bytesTemp3[2] = 0x20.toByte()
                            bytesTemp3[3] = 0x80.toByte()
                            System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.size)
                            //                        System.out.println("组包后 骑行： "+Arrays.toString(bytesTemp3));
                            val mModel = bytesTemp3[4]
                            val mSpeedLevel = bytesTemp3[5].toInt() and 0xFF
                            var mSpeedValue = bytesTemp3[6].toInt() and 0xFF
                            if (mSpeedValue != 0) {
                                mSpeedValue += 2
                            }
                            val mOffset = bytesTemp3[7].toInt() and 0xFF
                            val mSpasmNum = bytesTemp3[8].toInt() and 0xFF
                            val mSpasmLevel = bytesTemp3[9].toInt() and 0xFF
                            val mRes = bytesTemp3[10].toInt() and 0xFF
                            val mIntelligence = bytesTemp3[11]
                            val mDirection = bytesTemp3[12]
                            trySend(
                                ShangXiaZhi(
                                    model = mModel,
                                    speedLevel = mSpeedLevel,
                                    speedValue = mSpeedValue,
                                    offset = mOffset,
                                    spasmNum = mSpasmNum,
                                    spasmLevel = mSpasmLevel,
                                    res = mRes,
                                    intelligence = mIntelligence,
                                    direction = mDirection,
                                    medicalOrderId = medicalOrderId
                                )
                            )
                            buffer.discardReadBytes() //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                        } else {
                            buffer.resetReaderIndex()
                            continue
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // 解析游戏控制数据
            try {
                bufferShort.writeBytes(it)
                while (bufferShort.readableBytes() >= FRAME_LENGTH_SHORT) {
                    val bufTemp = bufferShort.readBytes(1)
                    val bytesTemp = ByteArray(1)
                    bufTemp.readBytes(bytesTemp)
                    if (bytesTemp[0] == 0xA3.toByte()) {
                        bufferShort.markReaderIndex()
                        val bufTemp1 = bufferShort.readBytes(3)
                        val bytesTemp1 = ByteArray(3)
                        bufTemp1.readBytes(bytesTemp1)
                        if (bytesTemp1[0] == 0x21.toByte() && bytesTemp1[1] == 0x20.toByte() && bytesTemp1[2] == 0x85.toByte()) {
                            //暂停
                            val bufTemp2 = bufferShort.readBytes(FRAME_LENGTH_SHORT - 4)
                            val bytesTemp2 = ByteArray(FRAME_LENGTH_SHORT - 4)
                            bufTemp2.readBytes(bytesTemp2)

                            //重新组帧
                            val bytesTemp3 = ByteArray(FRAME_LENGTH_SHORT)
                            bytesTemp3[0] = 0xA3.toByte()
                            bytesTemp3[1] = 0x21.toByte()
                            bytesTemp3[2] = 0x20.toByte()
                            bytesTemp3[3] = 0x85.toByte()
                            System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.size)
                            println("组包后 暂停： " + Arrays.toString(bytesTemp3))
                            // todo 暂停
                            bufferShort.discardReadBytes() //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                        } else if (bytesTemp1[0] == 0x21.toByte() && bytesTemp1[1] == 0x20.toByte() && bytesTemp1[2] == 0x86.toByte()) {
                            //停止
                            val bufTemp2 = bufferShort.readBytes(FRAME_LENGTH_SHORT - 4)
                            val bytesTemp2 = ByteArray(FRAME_LENGTH_SHORT - 4)
                            bufTemp2.readBytes(bytesTemp2)

                            //重新组帧
                            val bytesTemp3 = ByteArray(FRAME_LENGTH_SHORT)
                            bytesTemp3[0] = 0xA3.toByte()
                            bytesTemp3[1] = 0x21.toByte()
                            bytesTemp3[2] = 0x20.toByte()
                            bytesTemp3[3] = 0x86.toByte()
                            System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.size)
                            println("组包后 停止： " + Arrays.toString(bytesTemp3))
                            // todo 停止
                            bufferShort.discardReadBytes() //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                        } else {
                            bufferShort.resetReaderIndex()
                            continue
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
