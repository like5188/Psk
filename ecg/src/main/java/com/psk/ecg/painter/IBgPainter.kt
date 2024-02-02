package com.psk.ecg.painter

/**
 * 背景绘制者接口（包括标准方波的绘制）
 */
interface IBgPainter : IPainter {
    fun init(w: Int, h: Int, gridSize: Int, leadsCount: Int)

    /**
     * 是否绘制标准方波
     */
    fun hasStandardSquareWave(): Boolean
}