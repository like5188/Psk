package com.psk.device.util

import java.nio.ByteBuffer

class ShangXiaZhiDataParser {
    var receiver: ShangXiaZhiReceiver? = null

    fun putData(data: ByteArray?) {
        if (receiver == null || data == null) {
            return
        }
        // 骑行数据长度为13；暂停或者停止命令长度为5；有可能两种都有，及长度为18。
        when (data.size) {
            13 -> {
                parseData(data)
            }

            5 -> {
                parsePauseOrStop(data)
            }

            18 -> {
                // src:源数组;srcPos:源数组要复制的起始位置;dest:目的数组;destPos:目的数组放置的起始位置;length:复制的长度.
                val dat = ByteArray(13)
                System.arraycopy(data, 0, dat, 0, 13)
                parseData(dat)
                val cmd = ByteArray(5)
                System.arraycopy(data, 13, cmd, 0, 5)
                parsePauseOrStop(cmd)
            }
        }
    }

    /**
     * 解析骑行数据
     */
    private fun parseData(data: ByteArray) {
        println("parseData ${data.contentToString()}")
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
     * 解析暂停或者停止命令
     */
    private fun parsePauseOrStop(cmd: ByteArray) {
        println("parsePauseOrStop ${cmd.contentToString()}")
        val byteBuffer = ByteBuffer.wrap(cmd)
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
        speed: Int,
        offset: Int,
        spasmNum: Int,
        spasmLevel: Int,
        resistanceLevel: Int,
        intelligence: Byte,
        direction: Byte
    )

    fun onPause()

    fun onOver()
}
