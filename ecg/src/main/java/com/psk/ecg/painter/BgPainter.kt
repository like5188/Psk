package com.psk.ecg.painter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
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
    override fun init(w: Int, h: Int, gridSize: Int, leadsCount: Int) {
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas, h / gridSize, w, gridSize)
            drawVLine(canvas, w / gridSize, h, gridSize)
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
    private fun drawHLine(canvas: Canvas, count: Int, w: Int, gridSize: Int) {
        val startX = 0f
        val stopX = w.toFloat()
        (0..count).forEach {
            val y = it * gridSize.toFloat()
            if (it % 5 == 0) {
                canvas.drawLine(startX, y, stopX, y, solidLinePaint)
            } else {
                canvas.drawLine(startX, y, stopX, y, dashLinePaint)
            }
        }
    }

    // 画垂直线
    private fun drawVLine(canvas: Canvas, count: Int, h: Int, gridSize: Int) {
        val startY = 0f
        val stopY = h.toFloat()
        (0..count).forEach {
            val x = it * gridSize.toFloat()
            if (it % 5 == 0) {
                canvas.drawLine(x, startY, x, stopY, solidLinePaint)
            } else {
                canvas.drawLine(x, startY, x, stopY, dashLinePaint)
            }
        }
    }

    // 画标准方波。高度为10 mm，宽度为0.2 s（5 mm）
    private fun drawStandard(canvas: Canvas, h: Int, leadsCount: Int, gridSize: Int) {
        val paint = standardSquareWavePaint ?: return
        val path = Path()
        // 根据视图宽高计算
        val leadsH = h / leadsCount
        repeat(leadsCount) {
            // 计算标准方波
            val yOffset = leadsH / 2f + it * leadsH// x坐标轴移动到中间
            path.reset()
            path.moveTo(0f, yOffset)
            path.lineTo(gridSize * 5f, yOffset)
            path.lineTo(gridSize * 5f, yOffset - gridSize * 10f)
            path.lineTo(gridSize * 10f, yOffset - gridSize * 10f)
            path.lineTo(gridSize * 10f, yOffset)
            path.lineTo(gridSize * 15f, yOffset)
            canvas.drawPath(path, paint)
        }
    }

    companion object {
        val defaultBgPainter: BgPainter = BgPainter(Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
            alpha = 120
        }, Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
            alpha = 90
        }, Paint().apply {
            color = Color.parseColor("#ffffff")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = 125
        })
    }

}
