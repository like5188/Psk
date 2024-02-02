package com.psk.ecg.effect

import android.graphics.Path
import java.util.LinkedList

/**
 * 循环效果
 */
class CirclePathEffect : IPathEffect {
    // 循环效果时，不需要画线的数据的index。即视图中看起来是空白的部分。
    private var spaceIndex = 0
    override fun handleData(data: Float, drawDataList: LinkedList<Float>, maxDataCount: Int) {
        // 最多只绘制 maxDataCount 个数据
        if (drawDataList.size == maxDataCount) {
            drawDataList.removeAt(spaceIndex)
            drawDataList.add(spaceIndex, data)
            spaceIndex++
            if (spaceIndex == maxDataCount) {
                spaceIndex = 0
            }
        } else {
            drawDataList.addLast(data)
        }
    }

    override fun handlePath(path: Path, stepX: Float, index: Int, data: Float) {
        if (index == spaceIndex || index == 0) {// == 0 使用moveTo是为了在绘制标准方波时，不让方波终点和路径起点连接起来
            path.moveTo(index * stepX, data)// 达到空白效果
        } else {
            path.lineTo(index * stepX, data)
        }
    }

}
