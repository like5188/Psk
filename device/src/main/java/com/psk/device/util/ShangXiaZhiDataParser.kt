package com.psk.device.util

import java.nio.ByteBuffer

class ShangXiaZhiDataParser {
    var receiver: ShangXiaZhiReceiver? = null

    fun putData(data: ByteArray?) {
        if (receiver == null || data == null) {
            return
        }
        if (data.size == 5) {
            parsePauseOrStopData(data)
        } else if (data.size == 13) {
            parseData(data)
        }
    }

    /**
     * 解析骑行数据
     */
    private fun parseData(data: ByteArray) {
        val byteBuffer = ByteBuffer.wrap(data)
        if (byteBuffer.get() == 0xA3.toByte() && byteBuffer.get() == 0x21.toByte() && byteBuffer.get() == 0x20.toByte() && byteBuffer.get() == 0x80.toByte()) {
            val model: Byte = byteBuffer.get()
            val speedLevel: Int = byteBuffer.get().toInt()
            val speedValue: Int = byteBuffer.get().toInt()
            val offset: Int = byteBuffer.get().toInt()
            val spasmNum: Int = byteBuffer.get().toInt()
            val spasmLevel: Int = byteBuffer.get().toInt()
            val res: Int = byteBuffer.get().toInt()
            val intelligence: Byte = byteBuffer.get()
            val direction: Byte = byteBuffer.get()
            receiver?.onReceive(model, speedLevel, speedValue, offset, spasmNum, spasmLevel, res, intelligence, direction)
        }
    }

    /**
     * 解析暂停或者停止数据
     */
    private fun parsePauseOrStopData(data: ByteArray) {
        val byteBuffer = ByteBuffer.wrap(data)
        if (byteBuffer.get() == 0xA3.toByte() && byteBuffer.get() == 0x21.toByte() && byteBuffer.get() == 0x20.toByte()) {
            val byte = byteBuffer.get()
            if (byte == 0x85.toByte()) {
                receiver?.onPause()
            } else if (byte == 0x86.toByte()) {
                receiver?.onOver()
            }
        }
    }

}

interface ShangXiaZhiReceiver {
    fun onReceive(
        model: Byte,
        speedLevel: Int,
        speedValue: Int,
        offset: Int,
        spasmNum: Int,
        spasmLevel: Int,
        res: Int,
        intelligence: Byte,
        direction: Byte
    )

    fun onPause()

    fun onOver()
}
