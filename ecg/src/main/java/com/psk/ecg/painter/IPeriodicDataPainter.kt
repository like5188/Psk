package com.psk.ecg.painter

/**
 * 周期性绘制的绘制者接口
 */
interface IPeriodicDataPainter : IDataPainter {
    fun init(
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Int,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int,
        period: Long,
    )

    fun addData(data: List<Float>)

    /**
     * 是否有未绘制的数据
     */
    fun hasNotDrawData(): Boolean
}