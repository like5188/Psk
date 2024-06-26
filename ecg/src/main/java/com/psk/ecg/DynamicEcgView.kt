package com.psk.ecg

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.ecg.base.BaseEcgView
import com.psk.ecg.painter.IDataPainter
import com.psk.ecg.painter.IDynamicDataPainter
import com.psk.ecg.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * 动态心电图。周期性绘制。通常用于实时显示心电数据。
 */
class DynamicEcgView(context: Context, attrs: AttributeSet?) : BaseEcgView(context, attrs) {
    // 循环绘制任务
    private var job: Job? = null

    /**
     * 添加数据，用于循环绘制。
     * 当 surface 未创建时，不会添加数据。
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    fun addData(list: List<List<Float>>) {
        if (dataPainters.isNullOrEmpty()) {
            Log.e(TAG, "addData 失败，请先调用 setDataPainters")
            return
        }
        if (list.size != leadsCount) {
            Log.e(TAG, "addData 失败，和初始化时传入的导联数不一致！")
            return
        }
        // 在surface创建后，即可以绘制的时候，才允许添加数据，在surface销毁后禁止添加数据，以免造成数据堆积。
        if (!isSurfaceCreated) {
            Log.e(TAG, "addData 失败，isSurfaceCreated is false")
            return
        }
        dataPainters?.forEachIndexed { index, dataPainter ->
            Log.i(TAG, "addData 第 ${index + 1} 导联：${list[index].size}个数据 $list[index]")
            (dataPainter as IDynamicDataPainter).addData(list[index])
        }
        startDraw()// 有数据时启动任务
    }

    override fun startDraw() {
        if (!initialized) {
            Log.e(TAG, "startDraw failure, initialized false")
            return
        }
        if (!isSurfaceCreated) {
            Log.e(TAG, "startDraw failure, isSurfaceCreated false")
            return
        }
        if (job != null) {
            Log.e(TAG, "startDraw failure, job already start")
            return
        }
        val period = getPeriod()// 当第一次回调surfaceCreated()时，有可能没有此值。但是添加数据后会再次启动任务，所以这里不用使用阻塞。
        if (period <= 0L) {
            Log.e(TAG, "startDraw failure, period <= 0")
            return
        }
        Log.w(TAG, "startDraw period=$period")
        job = ViewTreeLifecycleOwner.get(this)?.lifecycleScope?.launch(Dispatchers.IO) {
            var canvas: Canvas? = null
            scheduleFlow(0, period).collect {
                // 这里和cancelJob方法都要加锁，避免前台切换到后台时，当一直有数据添加，
                // 那么 cancelJob 的时机有可能在 holder.lockCanvas 和 holder.unlockCanvasAndPost 方法之间，从而造成：
                // 1、java.lang.IllegalStateException: Surface has already been released.
                // 2、java.lang.IllegalArgumentException: Surface was already locked
                synchronized(this@DynamicEcgView) {
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

    private fun onDrawFrame(canvas: Canvas) {
        if (dataPainters!!.all { !(it as IDynamicDataPainter).hasNotDrawData() }) {
            doDraw(canvas)// 需要绘制背景。
            cancelJob("没有数据")// 没有数据时取消任务
            return
        }
        doDraw(canvas)
    }

    override fun cancelJob(cause: String) {
        synchronized(this@DynamicEcgView) {
            Log.w(TAG, "cancelJob $cause")
            job?.cancel()
            job = null
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        cancelJob("surfaceDestroyed")// 其实这里可以不必调用，因为没有数据时会调用
    }

    /**
     * 获取循环绘制周期间隔
     * @return
     * <=0：不进行绘制。
     * >0：按照此周期间隔循环绘制无限次。
     */
    private fun getPeriod(): Long = when {
        sampleRate <= 0 -> sampleRate.toLong()
        else -> {
            // 因为 scheduleFlow 循环任务在间隔时间太短会造成误差太多，
            // 在处理业务耗时太长时会造成丢帧（循环任务使用了conflate()操作符），如果丢帧造成数据堆积，会在PathPainter.draw()方法中处理。
            // 经测试，绘制能在30毫秒以内完成，这样绘制效果较好。为了能让它能被1000整除，这里选择25
            val interval = 1000 / sampleRate// 绘制每个数据的间隔时间。
            max(interval, 25).toLong()
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
        (dataPainter as IDynamicDataPainter).init(
            mm_per_mv,
            sampleRate,
            gridSize,
            stepX,
            xOffset,
            yOffset,
            maxShowNumbers,
            getPeriod(),
        )
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

}

