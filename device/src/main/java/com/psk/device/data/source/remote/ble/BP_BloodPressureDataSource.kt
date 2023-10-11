package com.psk.device.data.source.remote.ble

import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.Protocol
import com.psk.device.data.source.remote.ble.base.BaseBloodPressureDataSource
import kotlin.experimental.xor

/**
 * maibobo 血压计数据源
 */
class BP_BloodPressureDataSource : BaseBloodPressureDataSource() {
    override val protocol = Protocol("0000fff0-0000-1000-8000-00805f9b34fb",
        "0000fff1-0000-1000-8000-00805f9b34fb",
        "0000fff2-0000-1000-8000-00805f9b34fb",
        isBeginOfPacket = {
            /**
             * 注意：
             * 1、如果使用[fetch]方法，只会返回最终结果，所以根据[com.like.ble.central.connect.executor.BaseConnectExecutor.writeCharacteristicAndWaitNotify]中的处理逻辑，只用条件[最终结果.size == 20]解析没问题。
             * 2、如果使用[measure]方法，会返回所有实时结果[-86, -128, 2, 8, 1, 5, 0, 0, 0, 0, 68, 0, 74]和最终结果[-86, -128, 2, 15, 1, 6, 0, 23, 9, 21, 15, 1, 24, 0, 115, 0, 75, 0, 73, 102]，所以用条件[最终结果.size == 20]解析不出来。
             */
            // 最终测量结果[-86, -128, 2, 15, 1, 6, 0, 23, 9, 21, 15, 1, 24, 0, 115, 0, 75, 0, 73, 102]
            // 最终结果长度 20
            // 前导码 0xAA、0x80
            // 类型子码 0x06 表示最终结果。0x05表示实时结果
            it.size == 20 && it[0] == 0xAA.toByte() && it[1] == 0x80.toByte() && it[5] == 0x06.toByte()
        }) {
        it.lastOrNull() == calcCKS(it)
    }

    /**
     * 计算校验码
     */
    private fun calcCKS(bytes: ByteArray?): Byte {
        return if (bytes == null || bytes.isEmpty()) {
            0.toByte()
        } else {
            // 除前导码(前2个字节)、校验码(最后1个字节)之外的所有数据字节的异或值
            val data = bytes.copyOfRange(2, bytes.size - 1)
            var b = data[0]
            for (i in 1 until data.size) {
                b = b xor data[i]
            }
            b
        }
    }

    override suspend fun fetch(medicalOrderId: Long): BloodPressure? {
        val data = writeAndWaitResult("")
        return parseBloodPressure(data, medicalOrderId)
    }

    override suspend fun measure(medicalOrderId: Long): BloodPressure? {
        val data = writeAndWaitResult("cc80020301020002")
        return parseBloodPressure(data, medicalOrderId)
    }

    private fun parseBloodPressure(data: ByteArray?, medicalOrderId: Long): BloodPressure? {
        return if (data?.size == 20) {
            // 高8位左移8位+低8位。比如：高8位(0x01),低8位(0x78)。结果：0x01 shl 8 + 0x78 = 256 + 120 = 376
            val v0: Int = data[13].toInt() and 0xff shl 8
            val v1: Int = data[14].toInt() and 0xff
            val sbp: Int = v0 + v1
            val v2: Int = data[15].toInt() and 0xff shl 8
            val v3: Int = data[16].toInt() and 0xff
            val dbp: Int = v2 + v3
            BloodPressure(sbp = sbp, dbp = dbp, medicalOrderId = medicalOrderId)
        } else {
            null
        }
    }

    override suspend fun keepConnect(): Boolean {
        return write("cc80020301010001")
    }

}
