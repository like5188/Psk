package com.psk.ecg.painter

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.psk.ecg.effect.IPathEffect
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @param pathEffect    数据绘制效果。
 * 库中默认实现了两种：[CirclePathEffect]、[ScrollPathEffect]。你也可以自己实现[IPathEffect]接口。
 * @param paint         数据画笔。
 */
class DataPainter(
    private val pathEffect: IPathEffect, private val paint: Paint
) : IDataPainter {
    private val path = Path()
    private val notDrawDataQueue = ConcurrentLinkedQueue<Float>()// 未绘制的数据集合
    private val drawDataList = LinkedList<Float>()// 需要绘制的数据集合
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var xOffset = 0f// x轴偏移。因为绘制标准方波需要偏移路径的起始点
    private var stepX = 0f// x方向的步进，两个数据点在x轴方向的距离。px
    private var maxShowNumbers = 0// 整个视图能显示的最大数据量

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，所以每次多绘制几个，不让数据堆积太多造成界面延迟严重。
    private var numbersOfEachDraw = 0
    private var sampleRate = 0
    private var gridSize = 0
    private var mm_per_mv = 0

    override fun addData(data: List<Float>) {
        data.forEach {
            // 把uV电压值转换成y轴坐标值
            val mm = it * mm_per_mv// mV转mm
            val px = mm * gridSize// mm转px
            notDrawDataQueue.offer(px)// 入队成功返回true，失败返回false
        }
    }

    override fun hasNotDrawData(): Boolean = notDrawDataQueue.isNotEmpty()

    override fun init(
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
    ) {
        this.sampleRate = sampleRate
        this.gridSize = gridSize
        this.mm_per_mv = mm_per_mv
        // 根据采样率计算
        stepX = gridSize * mm_per_s / sampleRate.toFloat()
        // 根据视图宽高计算
        val leadsH = h / leadsCount
        yOffset = leadsH / 2f + leadsIndex * leadsH// x坐标轴移动到中间
        if (hasStandardSquareWave) {
            xOffset = gridSize * 15f// 3个大格
        }
        maxShowNumbers = ((w - xOffset) / stepX).toInt()
        // 为了让动画看起来没有延迟，即每秒钟绘制的数据基本达到采样率。
        val circleTimesPerSecond = (1000 / period).toInt()// 每秒绘制次数
        numbersOfEachDraw = sampleRate / circleTimesPerSecond
        Log.i(
            TAG,
            "第 ${leadsIndex + 1} 导联：stepX=$stepX yOffset=$yOffset maxShowNumbers=$maxShowNumbers numbersOfEachDraw=$numbersOfEachDraw"
        )
    }

    override fun draw(canvas: Canvas) {
        Log.v(TAG, "notDrawDataQueue=${notDrawDataQueue.size} drawDataList=${drawDataList.size}")
        if (notDrawDataQueue.isNotEmpty()) {
            repeat(
                // 如果剩余的数据量超过了 sampleRate，那么就每次多取1个数据，避免剩余数据量无限增长，造成暂停操作的延迟。
                if (notDrawDataQueue.size > sampleRate) {
                    numbersOfEachDraw + 1
                } else {
                    numbersOfEachDraw
                }
            ) {
                // 出队，空时返回null
                notDrawDataQueue.poll()?.let {
                    pathEffect.handleData(it, drawDataList, maxShowNumbers)
                }
            }
        }
        // 设置path
        path.reset()
        drawDataList.forEachIndexed { index, fl ->
            pathEffect.handlePath(path, stepX, index, fl)
        }
        path.offset(xOffset, yOffset)
        canvas.drawPath(path, paint)
    }

    companion object {
        private val TAG = DataPainter::class.java.simpleName
    }
}