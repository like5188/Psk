package com.psk.ecg.effect

import android.graphics.Path
import java.util.LinkedList

/**
 * 数据绘制效果
 */
interface IPathEffect {
    fun handleData(
        data: Float,
        drawDataList: LinkedList<Float>,
        maxDataCount: Int,
    )

    fun handlePath(
        path: Path, stepX: Float, index: Int, data: Float
    )
}
