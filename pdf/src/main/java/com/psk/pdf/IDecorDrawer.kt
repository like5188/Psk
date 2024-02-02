package com.psk.pdf

import android.graphics.Canvas

/**
 * 装饰绘制者接口。
 */
interface IDecorDrawer {

    /**
     * 画装饰区域，比如页眉页脚等
     */
    fun drawDecor(canvas: Canvas, pageWidth: Int, pageHeight: Int, page: Int, pageSize: Int, headerHeight: Int, footerHeight: Int)

}