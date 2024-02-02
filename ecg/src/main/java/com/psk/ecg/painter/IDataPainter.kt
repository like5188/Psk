package com.psk.ecg.painter

/**
 * 数据绘制者接口
 */
interface IDataPainter : IPainter {
    /**
     * @param leadsIndex                导联索引，从0开始。
     * @param hasStandardSquareWave     是否绘制标准方波。
     */
    fun init(
        mm_per_s: Int,
        mm_per_mv: Int,
        period: Long,
        sampleRate: Int,
        w: Int,
        h: Int,
        gridSize: Int,
        leadsCount: Int,
        leadsIndex: Int,
        hasStandardSquareWave: Boolean
    )

    fun addData(data: List<Float>)

    /**
     * 是否有未绘制的数据
     */
    fun hasNotDrawData(): Boolean
}