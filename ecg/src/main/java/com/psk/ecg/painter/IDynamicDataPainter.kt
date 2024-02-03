package com.psk.ecg.painter

/**
 * 动态心电图数据绘制接口
 */
interface IDynamicDataPainter : IDataPainter {
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