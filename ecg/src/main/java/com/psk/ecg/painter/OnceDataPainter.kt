package com.psk.ecg.painter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.psk.ecg.util.TAG
import java.util.LinkedList

/**
 * @param paint         数据画笔。
 */
class OnceDataPainter(private val paint: Paint) : IOnceDataPainter {
    private val path = Path()
    private val drawDataList = LinkedList<Float>()// 需要绘制的数据集合
    private var mm_per_mv = 0
    private var sampleRate = 0
    private var gridSize = 0
    private var stepX = 0f// x方向的步进，两个数据点在x轴方向的距离。px
    private var xOffset = 0f// x轴偏移。因为绘制标准方波需要偏移路径的起始点
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var maxShowNumbers = 0// 整个视图能显示的最大数据量

    override fun setData(data: List<Float>) {
        drawDataList.clear()
        data.mapTo(drawDataList) {
            mVToPx(it, mm_per_mv, gridSize)
        }
    }

    override fun init(
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Int,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    ) {
        this.mm_per_mv = mm_per_mv
        this.sampleRate = sampleRate
        this.gridSize = gridSize
        this.stepX = stepX
        this.xOffset = xOffset
        this.yOffset = yOffset
        this.maxShowNumbers = maxShowNumbers
    }

    override fun draw(canvas: Canvas) {
        Log.v(TAG, "draw drawDataList=${drawDataList.size}")
        // 设置path
        path.reset()
        drawDataList.take(maxShowNumbers).forEachIndexed { index, fl ->
            if (index == 0) {// == 0 使用moveTo是为了在绘制标准方波时，不让方波终点和路径起点连接起来
                path.moveTo(0f, fl)
            } else {
                path.lineTo(index * stepX, fl)
            }
        }
        path.offset(xOffset, yOffset)
        canvas.drawPath(path, paint)
    }

}
