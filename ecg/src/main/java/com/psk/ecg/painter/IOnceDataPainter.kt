package com.psk.ecg.painter

/**
 * 只绘制一次的绘制者接口
 */
interface IOnceDataPainter : IPainter {
    fun init(
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Int,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    )

    fun setData(data: List<Float>)
}