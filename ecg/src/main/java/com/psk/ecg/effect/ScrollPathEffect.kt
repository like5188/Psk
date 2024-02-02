package com.psk.ecg.effect

import android.graphics.Path
import java.util.LinkedList

/**
 * 滚动效果
 */
class ScrollPathEffect : IPathEffect {
    override fun handleData(data: Float, drawDataList: LinkedList<Float>, maxDataCount: Int) {
        // 最多只绘制 maxDataCount 个数据
        if (drawDataList.size == maxDataCount) {
            drawDataList.removeFirst()
        }
        drawDataList.addLast(data)
    }

    override fun handlePath(path: Path, stepX: Float, index: Int, data: Float) {
        if (index == 0) {// == 0 使用moveTo是为了在绘制标准方波时，不让方波终点和路径起点连接起来
            path.moveTo(0f, data)
        } else {
            path.lineTo(index * stepX, data)
        }
    }
}
