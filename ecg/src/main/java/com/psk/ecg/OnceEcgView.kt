package com.psk.ecg

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import com.psk.ecg.base.BaseEcgView
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.IBgPainter
import com.psk.ecg.painter.IOnceDataPainter
import com.psk.ecg.painter.OnceDataPainter
import com.psk.ecg.util.TAG
import kotlinx.coroutines.delay

/**
 * 一次性绘制所有数据。
 */
class OnceEcgView(context: Context, attrs: AttributeSet?) : BaseEcgView(context, attrs) {
    private lateinit var dataPainters: List<IOnceDataPainter>

    /**
     * @param sampleRate        采样率。
     * @param mm_per_s          走速（速度）。默认为标准值：25mm/s
     * @param mm_per_mv         增益（灵敏度）。默认为 1倍：10mm/mV
     * @param gridSize          一个小格子对应的像素。默认为设备实际 1mm对应的像素。
     * @param leadsCount        导联数量。默认为 1。
     * @param bgPainter         背景绘制者。默认为[BgPainter]。
     * 可以自己实现[IBgPainter]接口，或者自己创建[BgPainter]实例。
     * @param dataPainters      数据绘制者集合，有几个导联就需要几个绘制者。默认为包括[leadsCount]个[OnceDataPainter]的集合.
     * 可以自己实现[IOnceDataPainter]接口，或者自己创建[OnceDataPainter]实例。
     */
    fun init(
        sampleRate: Int,
        mm_per_s: Int = 25,
        mm_per_mv: Int = 10,
        gridSize: Int = (context.resources.displayMetrics.densityDpi / 25.4f).toInt(),
        leadsCount: Int = 1,
        bgPainter: IBgPainter? = BgPainter.defaultBgPainter,
        dataPainters: List<IOnceDataPainter> = (0 until leadsCount).map {
            OnceDataPainter.defaultDataPainter
        }
    ) {
        super.init(sampleRate, mm_per_s, mm_per_mv, gridSize, leadsCount, bgPainter)
        this.dataPainters = dataPainters
    }

    /**
     * 设置数据，只绘制一次，并且最多绘制不超过屏幕的数据量。
     * 注意：设置数据后，会阻塞等待 surface 创建完成，才开始绘制。
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    suspend fun setData(list: List<List<Float>>) {
        if (!::dataPainters.isInitialized) {
            Log.e(TAG, "setData 失败，请先调用 init 方法进行初始化")
            return
        }
        if (list.size != leadsCount) {
            Log.e(TAG, "setData 失败，和初始化时传入的导联数不一致！")
            return
        }
        // 等待surface创建完成，实际上是等待calcParams()执行完成后再添加数据。
        while (!isSurfaceCreated) {
            delay(10)
        }
        dataPainters.forEachIndexed { index, dataPainter ->
            Log.i(TAG, "setData 第 ${index + 1} 导联：${list[index].size}个数据")
            dataPainter.setData(list[index])
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
        if (!::dataPainters.isInitialized) {
            return
        }
        dataPainters[leadsIndex].init(mm_per_mv, sampleRate, gridSize, stepX, xOffset, yOffset, maxShowNumbers)
    }

    override fun onDrawData(canvas: Canvas) {
        dataPainters.forEach {
            it.draw(canvas)
        }
    }

}
