package com.psk.device.util

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