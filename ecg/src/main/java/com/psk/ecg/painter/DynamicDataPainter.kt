package com.psk.ecg.painter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.psk.ecg.effect.IPathEffect
import com.psk.ecg.util.TAG
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @param pathEffect    数据绘制效果。
 * 库中默认实现了两种：[com.psk.ecg.effect.CirclePathEffect]、[com.psk.ecg.effect.ScrollPathEffect]。你也可以自己实现[IPathEffect]接口。
 * @param paint         数据画笔。
 */
class DynamicDataPainter(private val pathEffect: IPathEffect, private val paint: Paint) : IDynamicDataPainter {
    private val path = Path()
    private val notDrawDataQueue = ConcurrentLinkedQueue<Float>()// 未绘制的数据集合
    private val drawDataList = LinkedList<Float>()// 需要绘制的数据集合
    private var mm_per_mv = 0
    private var sampleRate = 0
    private var gridSize = 0f
    private var stepX = 0f// x方向的步进，两个数据点在x轴方向的距离。px
    private var xOffset = 0f// x轴偏移。因为绘制标准方波需要偏移路径的起始点
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var maxShowNumbers = 0// 整个视图能显示的最大数据量

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，所以每次多绘制几个，不让数据堆积太多造成界面延迟严重。
    private var numbersOfEachDraw = 0
    private var circleTimesPerSecond = 0// 每秒绘制次数

    override fun addData(data: List<Float>) {
        data.forEach {
            notDrawDataQueue.offer(it)// 入队成功返回true，失败返回false
        }
    }

    override fun hasNotDrawData(): Boolean = notDrawDataQueue.isNotEmpty()

    override fun init(
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Float,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int,
        period: Long,
    ) {
        this.mm_per_mv = mm_per_mv
        this.sampleRate = sampleRate
        this.gridSize = gridSize
        this.stepX = stepX
        this.xOffset = xOffset
        this.yOffset = yOffset
        this.maxShowNumbers = maxShowNumbers
        if (period > 0) {// 周期有可能为0
            // 为了让动画看起来没有延迟，即每秒钟绘制的数据基本达到采样率。
            circleTimesPerSecond = (1000 / period).toInt()// 每秒绘制次数
            numbersOfEachDraw = sampleRate / circleTimesPerSecond
            Log.i(TAG, "period=$period circleTimesPerSecond=$circleTimesPerSecond numbersOfEachDraw=$numbersOfEachDraw")
        }
    }

    override fun draw(canvas: Canvas) {
        Log.v(TAG, "draw notDrawDataQueue=${notDrawDataQueue.size} drawDataList=${drawDataList.size}")
        if (notDrawDataQueue.isNotEmpty()) {
            val count = // 如果剩余的数据量超过了 sampleRate，那么就每次多取点数据，避免剩余数据量无限增长，造成暂停操作的延迟。
                if (notDrawDataQueue.size > sampleRate) {
                    numbersOfEachDraw + (notDrawDataQueue.size - sampleRate) / circleTimesPerSecond
                } else {
                    numbersOfEachDraw
                }
            Log.v(TAG, "draw 取出 $count 个数据")
            for (index in 0 until count) {
                // 出队，空时返回null
                val data = notDrawDataQueue.poll() ?: break
                pathEffect.handleData(data, drawDataList, maxShowNumbers)
            }
        }
        if (drawDataList.isEmpty()) {
            return
        }
        // 设置path
        path.reset()
        drawDataList.forEachIndexed { index, fl ->
            pathEffect.handlePath(path, stepX, index, mVToPx(fl, mm_per_mv, gridSize))
        }
        path.offset(xOffset, yOffset)
        canvas.drawPath(path, paint)
    }

}
