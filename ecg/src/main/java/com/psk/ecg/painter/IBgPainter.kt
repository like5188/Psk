package com.psk.ecg.painter

/**
 * 背景绘制接口（包括标准方波的绘制）
 */
interface IBgPainter : IPainter {
    fun init(w: Int, h: Int, gridSize: Float, leadsCount: Int, mm_per_s: Int, mm_per_mv: Int)

    /**
     * 获取x轴偏移量（如果绘制标准方波，则数据的绘制应该偏移）
     */
    fun getXOffset(): Float
}