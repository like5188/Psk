package com.psk.ecg.painter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * @param solidLinePaint            背景实线画笔。
 * @param dashLinePaint             背景虚线画笔。
 * @param standardSquareWavePaint   标准方波画笔。如果为 null，表示不绘制标准方波。
 */
class BgPainter(
    private val solidLinePaint: Paint, private val dashLinePaint: Paint, private val standardSquareWavePaint: Paint?
) : IBgPainter {
    private var bgBitmap: Bitmap? = null// 背景图片
    private var xOffset: Float = 0f

    /**
     * 创建背景图片
     */
    override fun init(w: Int, h: Int, gridSize: Float, leadsCount: Int, mm_per_s: Int, mm_per_mv: Int) {
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas, (h / gridSize).toInt() + 1, w, gridSize)
            drawVLine(canvas, (w / gridSize).toInt() + 1, h, gridSize)
            drawStandard(canvas, h, leadsCount, gridSize, mm_per_s, mm_per_mv)
        }
        xOffset = if (standardSquareWavePaint != null) {
            (2 + mm_per_s * 0.2f + 2) * gridSize
        } else {
            0f
        }
    }

    /**
     * 画背景图片
     */
    override fun draw(canvas: Canvas) {
        bgBitmap?.let {
            if (!it.isRecycled) {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    // 画水平线
    private fun drawHLine(canvas: Canvas, count: Int, w: Int, gridSize: Float) {
        val startX = 0f
        val stopX = w.toFloat()
        (0 until count).forEach {
            val y = it * gridSize
            if (it % 5 == 0) {
                canvas.drawLine(startX, y, stopX, y, solidLinePaint)
            } else {
                canvas.drawLine(startX, y, stopX, y, dashLinePaint)
            }
        }
    }

    // 画垂直线
    private fun drawVLine(canvas: Canvas, count: Int, h: Int, gridSize: Float) {
        val startY = 0f
        val stopY = h.toFloat()
        (0 until count).forEach {
            val x = it * gridSize
            if (it % 5 == 0) {
                canvas.drawLine(x, startY, x, stopY, solidLinePaint)
            } else {
                canvas.drawLine(x, startY, x, stopY, dashLinePaint)
            }
        }
    }

    // 画标准方波。高度为10 mm(1mV)，宽度为0.2 s（走纸速度为25mm/s）
    private fun drawStandard(canvas: Canvas, h: Int, leadsCount: Int, gridSize: Float, mm_per_s: Int, mm_per_mv: Int) {
        val paint = standardSquareWavePaint ?: return
        val path = Path()
        // 一个导联的高度
        val leadsH = h.toFloat() / leadsCount
        // 标准方波高度
        val standardSquareWaveHeight = mm_per_mv * gridSize
        // 标准方波宽度
        val standardSquareWaveWidth = mm_per_s * 0.2f * gridSize
        // 标准方波左边距
        val standardSquareWaveLeft = gridSize * 2
        // 标准方波右边距
        val standardSquareWaveRight = gridSize * 2
        repeat(leadsCount) {
            // 计算标准方波
            val yOffset = leadsH / 2 + it * leadsH// x坐标轴移动到中间
            path.reset()
            path.moveTo(0f, yOffset)
            path.lineTo(standardSquareWaveLeft, yOffset)
            path.lineTo(standardSquareWaveLeft, yOffset - standardSquareWaveHeight)
            path.lineTo(standardSquareWaveLeft + standardSquareWaveWidth, yOffset - standardSquareWaveHeight)
            path.lineTo(standardSquareWaveLeft + standardSquareWaveWidth, yOffset)
            path.lineTo(standardSquareWaveLeft + standardSquareWaveWidth + standardSquareWaveRight, yOffset)
            canvas.drawPath(path, paint)
        }
    }

    override fun getXOffset(): Float {
        return xOffset
    }

}
