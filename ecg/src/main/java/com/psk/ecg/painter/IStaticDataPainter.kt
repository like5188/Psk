package com.psk.ecg.painter

/**
 * 静态心电图数据绘制接口。
 */
interface IStaticDataPainter : IDataPainter {
    fun init(
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Float,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    )

    fun setData(data: List<Float>)
}