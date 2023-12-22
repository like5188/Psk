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
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max

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
    }

    private val bgPainter by lazy {
        BgPainter()
    }
    private val pathPainter by lazy {
        PathPainter(PathPainter.CirclePathEffect())
    }

    private var sampleRate = 0// 采样率
    private var gridSize = 0// 一个小格子对应的像素

    /**
     * @param sampleRate    采样率，为了让动画看起来没有延迟，即每秒钟绘制的数据基本达到采样率。
     * @param gridSize      一个小格子对应的像素，默认为设备实际1mm对应的像素。
     */
    fun init(sampleRate: Int, gridSize: Int = (context.resources.displayMetrics.densityDpi / 25.4f).toInt()) {
        Log.w(TAG, "init")
        this.sampleRate = sampleRate
        this.gridSize = gridSize
        calcParams(gridSize, sampleRate, width, height)
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
        Log.i(TAG, "gridSize=$gridSize sampleRate=$sampleRate w=$w h=$h MM_PER_S=$MM_PER_S MM_PER_MV=$MM_PER_MV")
        bgPainter.init(w, h, gridSize)
        pathPainter.init(MM_PER_S, MM_PER_MV, getPeriod(), gridSize, sampleRate, w, h)
    }

    override fun getPeriod(): Long {
        if (sampleRate <= 0) return 0L
        val interval = 1000 / sampleRate// 绘制每个数据的间隔时间。ceil向上取整
        // 因为 scheduleFlow 循环任务在间隔时间太短会造成误差太多，
        // 在处理业务耗时太长时会造成丢帧（循环任务使用了conflate()操作符），如果丢帧造成数据堆积，会在PathPainter.draw()方法中处理。
        // 经测试，绘制能在30毫秒以内完成，这样绘制效果较好。为了能让它能被1000整除，这里选择25
        return max(interval, 25).toLong()
    }

    /**
     * 添加数据，
     * @param data  需要添加的数据。mV。
     */
    fun addData(data: List<Float>) {
        // 在surface创建后，即可以绘制的时候，才允许添加数据，在surface销毁后禁止添加数据，以免造成数据堆积。
        if (data.isEmpty() || !isSurfaceCreated) return
        pathPainter.addData(data)
        startJob()// 有数据时启动任务
    }

    override fun onDrawFrame(canvas: Canvas) {
        Log.v(TAG, "onDrawFrame")
        if (!pathPainter.hasNotDrawData()) {
            cancelJob("没有数据")// 没有数据时取消任务
            return
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        bgPainter.draw(canvas)
        pathPainter.draw(canvas)
    }

}

class PathPainter(private val pathEffect: IPathEffect) {
    private val paint by lazy {
        Paint().apply {
            color = Color.parseColor("#44C71E")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }
    private val path = Path()
    private val notDrawDataQueue = ConcurrentLinkedQueue<Float>()// 未绘制的数据集合
    private val drawDataList = LinkedList<Float>()// 需要绘制的数据集合
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var stepX = 0f// x方向的步进，两个数据点在x轴方向的距离。px
    private var maxShowNumbers = 0// 整个视图能显示的最大数据量

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，所以每次多绘制几个，不让数据堆积太多造成界面延迟严重。
    private var numbersOfEachDraw = 0
    private var sampleRate = 0
    private var gridSize = 0
    private var MM_PER_MV = 0

    fun addData(data: List<Float>) {
        data.forEach {
            // 把uV电压值转换成y轴坐标值
            val mm = it * MM_PER_MV// mV转mm
            val px = mm * gridSize// mm转px
            notDrawDataQueue.offer(px)// 入队成功返回true，失败返回false
        }
    }

    fun hasNotDrawData(): Boolean = notDrawDataQueue.isNotEmpty()

    fun init(
        MM_PER_S: Int, MM_PER_MV: Int, period: Long, gridSize: Int, sampleRate: Int, w: Int, h: Int
    ) {
        this.sampleRate = sampleRate
        this.gridSize = gridSize
        this.MM_PER_MV = MM_PER_MV
        // 根据采样率计算
        stepX = gridSize * MM_PER_S / sampleRate.toFloat()
        // 根据视图宽高计算
        val hLineCount = h / gridSize// 水平线的数量
        val axisXCount = (hLineCount - hLineCount % 5) / 2// x坐标轴需要偏移的格数
        yOffset = axisXCount * gridSize.toFloat()
        maxShowNumbers = (w / stepX).toInt()
        val circleTimesPerSecond = (1000 / period).toInt()// 每秒绘制次数
        numbersOfEachDraw = sampleRate / circleTimesPerSecond
        Log.i(TAG, "stepX=$stepX yOffset=$yOffset maxShowNumbers=$maxShowNumbers numbersOfEachDraw=$numbersOfEachDraw")
    }

    private var max = 0// 辅助查看当前最大的未绘制数据量
    fun draw(canvas: Canvas) {
        if (notDrawDataQueue.isEmpty()) return
        if (notDrawDataQueue.size > max) {
            max = notDrawDataQueue.size
            Log.w(TAG, "maxNotDrawDataSize=$max")
        }
        Log.i(TAG, "notDrawDataQueue=${notDrawDataQueue.size} drawDataList=${drawDataList.size}")
        repeat(
            // 如果剩余的数据量超过了 sampleRate，那么就每次多取1个数据，避免剩余数据量无限增长，造成暂停操作的延迟。
            if (notDrawDataQueue.size > sampleRate) {
                numbersOfEachDraw + 1
            } else {
                numbersOfEachDraw
            }
        ) {
            // 出队，空时返回null
            notDrawDataQueue.poll()?.let {
                pathEffect.handleData(it, drawDataList, maxShowNumbers)
            }
        }
        // 设置path
        path.reset()
        drawDataList.forEachIndexed { index, fl ->
            pathEffect.handlePath(path, stepX, index, fl)
        }
        path.offset(0f, yOffset)
        canvas.drawPath(path, paint)
    }

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
            path.lineTo(index * stepX, data)
        }
    }

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
            if (index == spaceIndex) {
                path.moveTo(index * stepX, data)// 达到空白效果
            } else {
                path.lineTo(index * stepX, data)
            }
        }

    }
}

class BgPainter {
    private val paint by lazy {
        Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
        }
    }
    private var bgBitmap: Bitmap? = null// 背景图片
    private val dashPathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)// 虚线

    /**
     * 创建背景图片
     */
    fun init(w: Int, h: Int, gridSize: Int) {
        val hLineCount = h / gridSize// 水平线的数量
        val vLineCount = w / gridSize// 垂直线的数量
        Log.i(TAG, "hLineCount=$hLineCount vLineCount=$vLineCount")
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas, hLineCount, w, gridSize)
            drawVLine(canvas, vLineCount, h, gridSize)
        }
    }

    /**
     * 画背景图片
     */
    fun draw(canvas: Canvas) {
        bgBitmap?.let {
            if (!it.isRecycled) {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    // 画水平线
    private fun drawHLine(canvas: Canvas, count: Int, w: Int, gridSize: Int) {
        val startX = 0f
        val stopX = w.toFloat()
        (0..count).forEach {
            if (it % 5 == 0) {
                paint.pathEffect = null
                paint.alpha = 255
            } else {
                paint.pathEffect = dashPathEffect
                paint.alpha = 100
            }
            val y = it * gridSize.toFloat()
            canvas.drawLine(startX, y, stopX, y, paint)
        }
    }

    // 画垂直线
    private fun drawVLine(canvas: Canvas, count: Int, h: Int, gridSize: Int) {
        val startY = 0f
        val stopY = h.toFloat()
        (0..count).forEach {
            if (it % 5 == 0) {
                paint.pathEffect = null
                paint.alpha = 255
            } else {
                paint.pathEffect = dashPathEffect
                paint.alpha = 100
            }
            val x = it * gridSize.toFloat()
            canvas.drawLine(x, startY, x, stopY, paint)
        }
    }
}

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    // 循环绘制任务
    private var job: Job? = null
    protected var isSurfaceCreated = false

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
        isSurfaceCreated = false
        cancelJob("surfaceDestroyed")// 其实这里可以不必调用，因为没有数据时会调用
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    // activity onResume时调用
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w(TAG, "surfaceCreated")
        isSurfaceCreated = true
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

    protected fun cancelJob(cause: String) {
        Log.w(TAG, "cancelJob $cause")
        job?.cancel()
        job = null
    }

    /**
     * 绘制一帧
     */
    abstract fun onDrawFrame(canvas: Canvas)

    /**
     * 获取循环绘制周期间隔
     */
    abstract fun getPeriod(): Long
}

private const val TAG = "EcgChartViewTAG"
