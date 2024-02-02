package com.psk.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class DefaultDecorDrawer : IDecorDrawer {
    private val paint = Paint().apply {
        textSize = 50f
        color = Color.RED
    }

    override fun drawDecor(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        page: Int,
        pageSize: Int,
        headerHeight: Int,
        footerHeight: Int
    ) {
        // 页眉
        val headerY = headerHeight.toFloat()
        canvas.drawLine(0f, headerY, pageWidth.toFloat(), headerY, paint)
        // 页脚
        val footerY = (pageHeight - footerHeight).toFloat()
        canvas.drawLine(0f, footerY, pageWidth.toFloat(), footerY, paint)
        canvas.drawText("Page $page / $pageSize", pageWidth - 300f, footerY + 50f, paint)
    }

}