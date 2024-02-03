package com.psk.ecg.painter

interface IDataPainter : IPainter {

    /**
     * 把mV电压值转换成y轴坐标值px
     */
    fun mVToPx(mV: Float, mm_per_mv: Int, gridSize: Float): Float {
        val mm = mV * mm_per_mv// mV转mm
        return mm * gridSize// mm转px
    }
}