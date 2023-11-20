package com.psk.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {
    private var isDrawing = false
    private val renderThread by lazy {
        Thread(this)
    }

    init {
        holder.addCallback(this)
        // 画布透明处理
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun run() {
        var canvas: Canvas? = null
        while (isDrawing) {
            try {
                /*
                用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样就叫做双缓冲
                frontCanvas：实际显示的canvas。
                backCanvas：存储的是上一次更改前的canvas。
                 */
                canvas = holder.lockCanvas() ?: continue// 获取 backCanvas
                // 获取到的 Canvas 对象还是继续上次的 Canvas 对象，而不是一个新的 Canvas 对象。因此，之前的绘图操作都会被保留。
                // 在绘制前，通过 drawColor() 方法来进行清屏操作。
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                if (!onSurfaceViewDraw(canvas)) {
                    return
                }
            } finally {
                // 使用 backCanvas 替换 frontCanvas 作为新的 frontCanvas，原来的 frontCanvas 将切换到后台作为 backCanvas。
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * 绘制
     * @return true:持续循环绘制；false:停止循环绘制
     */
    abstract fun onSurfaceViewDraw(canvas: Canvas): Boolean

    /*
     下面的三个函数是 实现 SurfaceHolder.Callback 接口方法
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isDrawing = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isDrawing = true
        renderThread.start()
    }

}
