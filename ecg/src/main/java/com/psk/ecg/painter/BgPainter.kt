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

    /**
     * 创建背景图片
     */
    override fun init(w: Int, h: Int, gridSize: Float, leadsCount: Int) {
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas, (h / gridSize).toInt() + 1, w, gridSize)
            drawVLine(canvas, (w / gridSize).toInt() + 1, h, gridSize)
            drawStandard(canvas, h, leadsCount, gridSize)
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

    override fun hasStandardSquareWave(): Boolean {
        return standardSquareWavePaint != null
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

    // 画标准方波。高度为10 mm，宽度为0.2 s（5 mm）
    private fun drawStandard(canvas: Canvas, h: Int, leadsCount: Int, gridSize: Float) {
        val paint = standardSquareWavePaint ?: return
        val path = Path()
        // 根据视图宽高计算
        val leadsH = h / leadsCount
        repeat(leadsCount) {
            // 计算标准方波
            val yOffset = leadsH / 2f + it * leadsH// x坐标轴移动到中间
            path.reset()
            path.moveTo(0f, yOffset)
            path.lineTo(gridSize * 5, yOffset)
            path.lineTo(gridSize * 5, yOffset - gridSize * 10)
            path.lineTo(gridSize * 10, yOffset - gridSize * 10)
            path.lineTo(gridSize * 10, yOffset)
            path.lineTo(gridSize * 15, yOffset)
            canvas.drawPath(path, paint)
        }
    }

}
