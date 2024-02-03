package com.psk.ecg

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import com.psk.ecg.base.BaseEcgView
import com.psk.ecg.painter.IDataPainter
import com.psk.ecg.painter.IStaticDataPainter
import com.psk.ecg.painter.StaticDataPainter
import com.psk.ecg.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 静态心电图。一次性绘制所有数据。通常用于报告页面显示。
 */
class StaticEcgView(context: Context, attrs: AttributeSet?) : BaseEcgView(context, attrs) {

    /**
     * 设置数据，只绘制一次，并且最多绘制不超过屏幕的数据量。
     * 注意：设置数据后，会挂起等待 surface 创建完成，才开始绘制。
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    suspend fun setData(list: List<List<Float>>) = withContext(Dispatchers.IO) {
        if (!initialized()) {
            Log.e(TAG, "setData 失败，请先调用 init 方法进行初始化")
            return@withContext
        }
        if (list.size != leadsCount) {
            Log.e(TAG, "setData 失败，和初始化时传入的导联数不一致！")
            return@withContext
        }
        // 等待surface创建完成，实际上是等待calcParams()执行完成后再添加数据。
        while (!isSurfaceCreated) {
            delay(10)
        }
        dataPainters.forEachIndexed { index, dataPainter ->
            Log.i(TAG, "setData 第 ${index + 1} 导联：${list[index].size}个数据")
            (dataPainter as IStaticDataPainter).setData(list[index])
        }
        startDraw()
    }

    private fun startDraw() {
        Log.w(TAG, "startDraw")
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas()
            canvas?.let {
                doDraw(it)
            }
        } finally {
            canvas?.let {
                try {
                    holder.unlockCanvasAndPost(it)
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun onInitData(
        leadsIndex: Int,
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Int,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    ) {
        (dataPainters[leadsIndex] as IStaticDataPainter).init(mm_per_mv, sampleRate, gridSize, stepX, xOffset, yOffset, maxShowNumbers)
    }

    override fun getDefaultDataPainter(): IDataPainter {
        return StaticDataPainter(Paint().apply {
            color = Color.parseColor("#44C71E")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        })
    }

}
