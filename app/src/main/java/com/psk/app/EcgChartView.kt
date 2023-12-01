package com.psk.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.like.common.util.dp
import com.like.common.util.sp
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil

/*
    SurfaceView 是一个可以在子线程中更新 UI 的 View，且不会影响到主线程。它为自己创建了一个窗口（window），就好像在视图层次（View Hierarchy）上穿了个“洞”，让绘图层（Surface）直接显示出来。但是，和常规视图（view）不同，它没有动画或者变形特效，一些 View 的特性也无法使用。
    概括：
    SurfaceView 独立于视图层次（View Hierarchy），拥有自己的绘图层（Surface），但也没有一些常规视图（View）的特性，如动画等。
    SurfaceView 的实现中具有两个绘图层（Surface），即我们所说的双缓冲机制。我们的绘制发生在后台画布上，并通过交换前后台画布来刷新画面，可避免局部刷新带来的闪烁，也提高了渲染效率。
    SurfaceView 中的 SurfaceHolder 是 Surface 的持有者和管理控制者。
    SurfaceHolder.Callback 的各个回调发生在主线程

    实际心电图纸是由1mm*1mm的小方格组成，每1大格分为5小格
    横坐标代表时间，一般采用的走纸速度为25mm/s，每小格代表0.04s。
    纵坐标代表电压，一般采用的定标电压为1mV=10mm（10小格），每小格代表0.1mV。
    1倍电压：1uV=10mm；1/2电压：1uV=5mm；2倍电压：1uV=20mm
    注意：如果采用非1倍电压，在计算结果时需要还原。
 */
class EcgChartView(context: Context, attrs: AttributeSet?) : AbstractSurfaceView(context, attrs) {
    companion object {
        private const val MM_PER_S = 25// 走速（速度）。默认为标准值：25mm/s
        private const val MM_PER_MV = 10// 增益（灵敏度）。默认为1倍：10mm/mV
        private const val DRAW_TEXT = "${MM_PER_S}mm/s  ${MM_PER_MV}mm/mV"
    }

    // 画文字的画笔
    private val textPaint by lazy {
        TextPaint().apply {
            color = Color.parseColor("#88ffffff")
            textSize = 12f.sp
            strokeWidth = 1f
            isAntiAlias = true
        }
    }

    // 画网格的画笔
    private val gridPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
        }
    }

    // 画心电数据曲线的画笔
    private val dataPaint by lazy {
        Paint().apply {
            color = Color.parseColor("#44C71E")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    private var sampleRate = 0// 采样率
    private var gridSize = 0// 一个小格子对应的像素

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，造成界面延迟严重。
    // 因为 scheduleFlow 循环任务在间隔时间太短或者处理业务耗时太长时会造成误差太多。
    // 经测试，大概16毫秒以上循环误差就比较小了，建议使用30毫秒以上，这样绘制效果较好。
    private var drawDataCountEachTime = 0
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var stepX = 0f// x方向的步进，两个数据在x轴方向的距离。px
    private var maxDataCount = 0// 能显示的最大数据量
    private var bgBitmap: Bitmap? = null// 背景图片
    private val notDrawDataQueue = ConcurrentLinkedQueue<Float>()// 未绘制的数据集合
    private val drawDataList = LinkedList<Float>()// 需要绘制的数据集合
    private val path = Path()
    private val isInitialized = AtomicBoolean(false)

    /**
     * @param sampleRate    采样率，为了让动画看起来没有延迟，即每秒钟绘制的数据基本达到采样率。
     * @param gridSize      一个小格子对应的像素，默认为设备实际1mm对应的像素。
     */
    fun init(sampleRate: Int, gridSize: Int = (context.resources.displayMetrics.densityDpi / 25.4f).toInt()) {
        if (isInitialized.compareAndSet(false, true)) {
            Log.w(TAG, "init")
            this.sampleRate = sampleRate
            this.gridSize = gridSize
            calcParams(gridSize, sampleRate, width, height)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.w(TAG, "onSizeChanged")
        calcParams(gridSize, sampleRate, w, h)
    }

    // 计算相关参数
    private fun calcParams(gridSize: Int, sampleRate: Int, w: Int, h: Int) {
        if (gridSize <= 0 || sampleRate <= 0 || w <= 0 || h <= 0) {
            return
        }
        // 根据采样率计算
        stepX = gridSize * MM_PER_S / sampleRate.toFloat()
        val interval = 1000 / sampleRate// 绘制每个数据的间隔时间
        val recommendInterval = 30.0// 建议循环间隔时间
        drawDataCountEachTime = if (interval < recommendInterval) ceil(recommendInterval / interval).toInt() else interval// Math.ceil()向上取整
        Log.i(TAG, "gridSize=$gridSize sampleRate=$sampleRate stepX=$stepX drawDataCountEachTime=$drawDataCountEachTime")

        // 根据视图宽高计算
        val hLineCount = h / gridSize// 水平线的数量
        val vLineCount = w / gridSize// 垂直线的数量
        val axisXCount = (hLineCount - hLineCount % 5) / 2// x坐标轴需要偏移的格数
        yOffset = axisXCount * gridSize.toFloat()
        maxDataCount = (w / stepX).toInt()
        // 绘制背景到bitmap中
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas, hLineCount, w)
            drawVLine(canvas, vLineCount, h)
        }
        Log.i(
            TAG,
            "w=$w h=$h hLineCount=$hLineCount vLineCount=$vLineCount axisXCount=$axisXCount yOffset=$yOffset maxDataCount=$maxDataCount"
        )
    }

    override fun getPeriod(): Long = if (sampleRate > 0 && drawDataCountEachTime > 0) {
        1000L / sampleRate * drawDataCountEachTime
    } else {
        0L
    }

    /**
     * 添加数据，数据的单位是 mV。
     */
    fun addData(data: List<Float>) {
        // 在surface创建后，即可以绘制的时候，才允许添加数据，在surface销毁后禁止添加数据，以免造成数据堆积。
        if (data.isEmpty() || !isCreated) return
        data.forEach {
            // 把uV电压值转换成y轴坐标值
            val mm = it * MM_PER_MV// mV转mm
            val px = mm * gridSize// mm转px
            notDrawDataQueue.offer(px)// 入队成功返回true，失败返回false
        }
        startJob()// 有数据时启动任务
    }

    override fun onCircleDraw(canvas: Canvas) {
        Log.v(TAG, "onCircleDraw")
        if (notDrawDataQueue.isEmpty()) {
            cancelJob("没有数据")// 没有数据时取消任务
            return
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//        drawBg(canvas)
        drawText(canvas)
        drawData(canvas)
    }

    // 画背景图片
    private fun drawBg(canvas: Canvas) {
        if (bgBitmap?.isRecycled == false) {
            canvas.drawBitmap(bgBitmap!!, 0f, 0f, null)
        }
    }

    // 画文字
    private fun drawText(canvas: Canvas) {
        canvas.drawText(DRAW_TEXT, 20f.dp, height.toFloat() - 10.dp, textPaint)
    }

    // 画心电数据
    private fun drawData(canvas: Canvas) {
        calcScrollPath()
        canvas.drawPath(path, dataPaint)
    }

    // 循环效果时，不需要画线的数据的index。即视图中看起来是空白的部分。
    private var spaceIndex = 0

    /**
     * 循环效果
     */
    private fun calcCirclePath() {
        if (notDrawDataQueue.isEmpty()) return
        // 总共需要取出 drawDataCountEachTime 个数据
        repeat(drawDataCountEachTime) {
            // 出队，空时返回null
            notDrawDataQueue.poll()?.let {
                // 最多只绘制 maxDataCount 个数据
                if (drawDataList.size == maxDataCount) {
                    drawDataList.removeAt(spaceIndex)
                    drawDataList.add(spaceIndex, it)
                    spaceIndex++
                    if (spaceIndex == maxDataCount) {
                        spaceIndex = 0
                    }
                } else {
                    drawDataList.addLast(it)
                }
            }
        }
        // 设置path
        path.reset()
        drawDataList.forEachIndexed { index, fl ->
            if (index == spaceIndex) {
                path.moveTo(index * stepX, fl)// 达到空白效果
            } else {
                path.lineTo(index * stepX, fl)
            }
        }
        path.offset(0f, yOffset)
    }

    /**
     * 滚动效果
     */
    private fun calcScrollPath() {
        if (notDrawDataQueue.isEmpty()) return
        // 总共需要取出 drawDataCountEachTime 个数据
        repeat(drawDataCountEachTime) {
            // 出队，空时返回null
            notDrawDataQueue.poll()?.let {
                // 最多只绘制 maxDataCount 个数据
                if (drawDataList.size == maxDataCount) {
                    drawDataList.removeFirst()
                }
                drawDataList.addLast(it)
            }
        }
        // 设置path
        path.reset()
        drawDataList.forEachIndexed { index, fl ->
            path.lineTo(index * stepX, fl)
        }
        path.offset(0f, yOffset)
    }

    // 画水平线
    private fun drawHLine(canvas: Canvas, count: Int, w: Int) {
        val dashPathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)// 虚线
        val startX = 0f
        val stopX = w.toFloat()
        (0..count).forEach {
            if (it % 5 == 0) {
                gridPaint.pathEffect = null
                gridPaint.alpha = 255
            } else {
                gridPaint.pathEffect = dashPathEffect
                gridPaint.alpha = 100
            }
            val y = it * gridSize.toFloat()
            canvas.drawLine(startX, y, stopX, y, gridPaint)
        }
    }

    // 画垂直线
    private fun drawVLine(canvas: Canvas, count: Int, h: Int) {
        val dashPathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)// 虚线
        val startY = 0f
        val stopY = h.toFloat()
        (0..count).forEach {
            if (it % 5 == 0) {
                gridPaint.pathEffect = null
                gridPaint.alpha = 255
            } else {
                gridPaint.pathEffect = dashPathEffect
                gridPaint.alpha = 100
            }
            val x = it * gridSize.toFloat()
            canvas.drawLine(x, startY, x, stopY, gridPaint)
        }
    }

}

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    // 循环绘制任务
    private var job: Job? = null
    protected var isCreated = false

    init {
        holder.addCallback(this)
        // 画布透明处理
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    /*
     下面的三个函数是 实现 SurfaceHolder.Callback 接口方法
     */
    // activity onPause时调用
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.w(TAG, "surfaceDestroyed")
        isCreated = false
        cancelJob("surfaceDestroyed")// 其实这里可以不必调用，因为没有数据时会调用
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    // activity onResume时调用
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w(TAG, "surfaceCreated")
        isCreated = true
        startJob()// 其实这里可以不必调用，因为有数据时会调用
    }

    protected fun startJob() {
        if (job != null) return
        Log.w(TAG, "startJob")
        job = findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
            var canvas: Canvas? = null
            // 阻塞等待period值
            val period = getPeriod()// 当第一次回调surfaceCreated()时，有可能没有此值。但是添加数据后会再次启动任务，所以这里不用使用阻塞。
            if (period <= 0L) {
                cancelJob("period=$period")
                return@launch
            }
            Log.w(TAG, "开始循环绘制任务 period=$period")
            scheduleFlow(0, period).collect {
                try {
                    // 用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样就叫做双缓冲
                    // frontCanvas：实际显示的canvas。
                    // backCanvas：存储的是上一次更改前的canvas。
                    canvas = holder.lockCanvas() // 获取 backCanvas
                    // 获取到的 Canvas 对象还是继续上次的 Canvas 对象，而不是一个新的 Canvas 对象。因此，之前的绘图操作都会被保留。
                    // 所以，在绘制前，需要通过 drawColor() 方法来进行清屏操作。
                    canvas?.let {
                        onCircleDraw(it)
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

    protected fun cancelJob(cause: String) {
        Log.w(TAG, "cancelJob $cause")
        job?.cancel()
        job = null
    }

    /**
     * 循环绘制
     */
    abstract fun onCircleDraw(canvas: Canvas)

    /**
     * 获取循环绘制周期间隔
     */
    abstract fun getPeriod(): Long
}

private const val TAG = "EcgChartViewTAG"
