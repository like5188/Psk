package com.psk.ecg.effect

import android.graphics.Path
import java.util.LinkedList

/**
 * 循环效果
 * @param spaceDataCount    空白数据数量。数值越大，空白越长。
 */
class CirclePathEffect(private val spaceDataCount: Int = 10) : IPathEffect {
    private var spaceIndex = 0// 循环效果时，不需要画线的数据的index。即视图中看起来是空白的部分。
    private var isFull = false// 是否是满数据状态

    override fun init() {
        spaceIndex = 0
        isFull = false
    }

    override fun handleData(data: Float, drawDataList: LinkedList<Float>, maxDataCount: Int) {
        isFull = drawDataList.size == maxDataCount
        // 最多只绘制 maxDataCount 个数据
        if (isFull) {
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
        if (isFull) {// 满数据状态
            if (index == spaceIndex + spaceDataCount) {// 空白效果
                path.moveTo(index * stepX, data)
            } else if (index !in spaceIndex until spaceIndex + spaceDataCount) {// spaceDataCount 个数据不做任何处理，任何直接跳到 spaceIndex + spaceDataCount
                path.lineTo(index * stepX, data)
            }
        } else {// 非满数据状态
            if (index == 0) {
                // 需要使用moveTo是为了在绘制标准方波时，不让方波终点和路径起点连接起来
                path.moveTo(index * stepX, data)
            } else {
                path.lineTo(index * stepX, data)
            }
        }
    }

}
