package com.psk.ecg

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import com.psk.ecg.base.BaseParamsSurfaceView
import com.psk.ecg.util.TAG
import kotlinx.coroutines.delay

class OnceSurfaceView(context: Context, attrs: AttributeSet?) : BaseParamsSurfaceView(context, attrs) {
    /**
     * 设置数据，只绘制一次，并且最多绘制不超过屏幕的数据量。
     * 注意：设置数据后，会阻塞等待 surface 创建完成，才开始绘制。
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    suspend fun setData(list: List<List<Float>>) {
        if (!initialized()) {
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

    override fun getPeriod(): Long {
        return 0L
    }

}
