package com.psk.ecg

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.ecg.base.BaseEcgView
import com.psk.ecg.painter.IDataPainter
import com.psk.ecg.painter.IStaticDataPainter
import com.psk.ecg.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 静态心电图。一次性绘制所有数据。通常用于报告页面显示。
 */
class StaticEcgView(context: Context, attrs: AttributeSet?) : BaseEcgView(context, attrs) {

    /**
     * 设置数据，只绘制一次，并且最多绘制不超过屏幕的数据量。
     * 注意：设置数据后，会挂起等待 surface 创建完成并且设置参数完成，才开始绘制。
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    fun setData(list: List<List<Float>>) {
        if (dataPainters.isNullOrEmpty()) {
            Log.e(TAG, "setData 失败，请先调用 setDataPainters")
            return
        }
        if (list.size != leadsCount) {
            Log.e(TAG, "setData 失败，和初始化时传入的导联数不一致！")
            return
        }
        dataPainters?.forEachIndexed { index, dataPainter ->
            Log.i(TAG, "setData 第 ${index + 1} 导联：${list[index].size}个数据")
            (dataPainter as IStaticDataPainter).setData(list[index])
        }
        startDraw()
    }

    override fun startDraw() {
        if (!initialized) return
        if (!isSurfaceCreated) return
        Log.w(TAG, "startDraw")
        ViewTreeLifecycleOwner.get(this)?.lifecycleScope?.launch(Dispatchers.IO) {
            doDraw()
        }
    }

    override fun onInitData(
        dataPainter: IDataPainter,
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Float,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    ) {
        (dataPainter as IStaticDataPainter).init(mm_per_mv, sampleRate, gridSize, stepX, xOffset, yOffset, maxShowNumbers)
    }

}
