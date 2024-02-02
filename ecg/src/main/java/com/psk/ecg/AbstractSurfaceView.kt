package com.psk.ecg

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    // 循环绘制任务
    private var job: Job? = null
    protected var isSurfaceCreated = false

    init {
        holder.addCallback(this)
    }

    /*
     下面的三个函数是 实现 SurfaceHolder.Callback 接口方法
     */
    // activity onPause时调用
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.w("EcgChartView", "surfaceDestroyed")
        isSurfaceCreated = false
        cancelJob("surfaceDestroyed")// 其实这里可以不必调用，因为没有数据时会调用
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.w("EcgChartView", "surfaceChanged")
    }

    // activity onResume时调用
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w("EcgChartView", "surfaceCreated")
        isSurfaceCreated = true
    }

    protected fun startJob() {
        if (!isSurfaceCreated) return
        if (job != null) return
        val period = getPeriod()// 当第一次回调surfaceCreated()时，有可能没有此值。但是添加数据后会再次启动任务，所以这里不用使用阻塞。
        if (period < 0L) {
            return
        }
        if (period == 0L) {// 只绘制一次
            Log.w("EcgChartView", "startJob 只绘制一次")
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let {
                    onDrawFrame(it)
                }
            } finally {
                canvas?.let {
                    try {
                        holder.unlockCanvasAndPost(it)
                    } catch (e: Exception) {
                    }
                }
            }
            return
        }
        Log.w("EcgChartView", "startJob 循环绘制 period=$period")
        job = ViewTreeLifecycleOwner.get(this)?.lifecycleScope?.launch(Dispatchers.IO) {
            var canvas: Canvas? = null
            scheduleFlow(0, period).collect {
                // 这里和cancelJob方法都要加锁，避免前台切换到后台时，当一直有数据添加，
                // 那么 cancelJob 的时机有可能在 holder.lockCanvas 和 holder.unlockCanvasAndPost 方法之间，从而造成：
                // 1、java.lang.IllegalStateException: Surface has already been released.
                // 2、java.lang.IllegalArgumentException: Surface was already locked
                synchronized(this@AbstractSurfaceView) {
                    try {
                        // 用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样就叫做双缓冲
                        // frontCanvas：实际显示的canvas。
                        // backCanvas：存储的是上一次更改前的canvas。
                        canvas = holder.lockCanvas() // 获取 backCanvas
                        // 获取到的 Canvas 对象还是继续上次的 Canvas 对象，而不是一个新的 Canvas 对象。因此，之前的绘图操作都会被保留。
                        // 所以，在绘制前，需要通过 drawColor() 方法来进行清屏操作。
                        canvas?.let {
                            onDrawFrame(it)
                        }
                    } finally {
                        canvas?.let {
                            // 使用 backCanvas 替换 frontCanvas 作为新的 frontCanvas，原来的 frontCanvas 将切换到后台作为 backCanvas。
                            try {
                                holder.unlockCanvasAndPost(it)
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }

    protected fun cancelJob(cause: String) {
        synchronized(this@AbstractSurfaceView) {
            Log.w("EcgChartView", "cancelJob $cause")
            job?.cancel()
            job = null
        }
    }

    /**
     * 延时准确的循环回调flow
     * @param delay         第一次执行时延迟多久，毫秒
     * @param period        循环执行周期，毫秒
     * @param count         循环次数。默认0。小于等于0表示不限制，无限循环
     */
    private fun scheduleFlow(delay: Long, period: Long, count: Int = 0): Flow<Long> =
        flow {
            val startTime = System.currentTimeMillis() + delay
            var i = 0
            while (count <= 0 || i < count) {
                emit(startTime + period * i++)
            }
        }
            .buffer()// 使用buffer操作符建立一个有64个位置的缓冲区,如果发送时发现缓冲区满了,就会挂起等待缓冲区有可用位置后再发送
            .transform {
                // 这里没有使用 delay(it - System.currentTimeMillis())，因为误差比较大。
                while (true) {
                    val time = System.currentTimeMillis()
                    if (time >= it) {
                        emit(time)
                        break
                    }
                }
            }
            .conflate()
            .flowOn(Dispatchers.Default)

    /**
     * 绘制一帧
     */
    abstract fun onDrawFrame(canvas: Canvas)

    /**
     * 获取循环绘制周期间隔
     * @return
     * ==0：表示只绘制一次。此时只绘制不超过屏幕的所有数据。
     * <0：不进行绘制。
     * >0：按照此周期间隔循环绘制无限次。
     */
    abstract fun getPeriod(): Long

}

