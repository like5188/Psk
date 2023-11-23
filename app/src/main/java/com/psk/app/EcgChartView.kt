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
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.math.ceil

/*
    实际心电图纸是由1mm*1mm的小方格组成，每1大格分为5小格
    横坐标代表时间，一般采用的走纸速度为25mm/s，每小格代表0.04s。
    纵坐标代表电压，一般采用的定标电压为1mV=10mm（10小格），每小格代表0.1mV。
    1倍电压：1uV=10mm；1/2电压：1uV=5mm；2倍电压：1uV=20mm
    注意：如果采用非1倍电压，在计算结果时需要还原。
 */
class EcgChartView(context: Context, attrs: AttributeSet?) : AbstractSurfaceView(context, attrs) {
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
    private val drawBg = false// 是否绘制背景网格

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，造成界面延迟严重。
    // 因为 scheduleFlow 循环任务在间隔时间太短或者处理业务耗时太长时会造成误差太多。
    // 经测试，大概16毫秒以上循环误差就比较小了，建议使用30毫秒以上，这样绘制效果较好。
    // Math.ceil()向上取整
    private var drawDataCountEachTime = 0
    private var period = 0L// 循环绘制周期间隔
    private var sampleRate = 0// 采样率

    private var gridSpace = 0// 一个小格子对应的像素，即1mm对应的像素。px/mm
    private var hLineCount = 0// 水平线的数量
    private var vLineCount = 0// 垂直线的数量
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var stepX = 0f// x方向的步进，两个数据在x轴方向的距离。px
    private var maxDataCount = 0// 能显示的最大数据量

    private var bgBitmap: Bitmap? = null// 背景图片
    private val dashPathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)// 虚线
    private val notDrawDataList = mutableListOf<Float>()// 未绘制的数据集合
    private val drawDataList = mutableListOf<Float>()// 需要绘制的数据集合
    private val dataPath = Path()

    // 保证calcParams方法执行之后才执行scheduleFlow(0, period)，因为后者依赖前者的period。
    private val countDownLatch by lazy {
        CountDownLatch(1)
    }

    init {
        // 1mm对应的像素值
        gridSpace = (context.resources.displayMetrics.densityDpi / 25.4f).toInt()
        println("MM_PER_S=$MM_PER_S MM_PER_MV=$MM_PER_MV gridSpace=$gridSpace")
    }

    /**
     * @param sampleRate    采样率
     */
    fun init(sampleRate: Int) {
        println("init")
        this.sampleRate = sampleRate
        calcParams(sampleRate, width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        println("onSizeChanged")
        calcParams(sampleRate, w, h)
    }

    // 计算相关参数
    private fun calcParams(sampleRate: Int, w: Int, h: Int) {
        if (sampleRate <= 0 || w <= 0 || h <= 0) {
            return
        }
        // 根据采样率计算
        stepX = gridSpace * MM_PER_S / sampleRate.toFloat()
        val interval = 1000 / sampleRate// 绘制每个数据的间隔时间
        val recommendInterval = 30.0// 建议循环间隔时间
        drawDataCountEachTime = if (interval < recommendInterval) ceil(recommendInterval / interval).toInt() else interval
        period = 1000L / sampleRate * drawDataCountEachTime
        println("sampleRate=$sampleRate stepX=$stepX drawDataCountEachTime=$drawDataCountEachTime period=$period")

        // 根据视图宽高计算
        hLineCount = h / gridSpace
        vLineCount = w / gridSpace
        val axisXCount = (hLineCount - hLineCount % 5) / 2
        yOffset = axisXCount * gridSpace.toFloat()
        maxDataCount = (w / stepX).toInt()
        // 绘制背景到bitmap中
        if (drawBg) {
            bgBitmap?.recycle()
            bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                drawHLine(canvas)
                drawVLine(canvas)
            }
        }
        println("w=$w h=$h hLineCount=$hLineCount vLineCount=$vLineCount axisXCount=$axisXCount yOffset=$yOffset maxDataCount=$maxDataCount")
        countDownLatch.countDown()
    }

    /**
     * 添加数据，数据的单位是 mV。
     */
    fun addData(data: List<Float>) {
        if (data.isEmpty()) return
        notDrawDataList.addAll(data.map {
            // 把uV电压值转换成y轴坐标值
            val mm = it * MM_PER_MV// mV转mm
            mm * gridSpace// mm转px
        })
        startCircleDrawJob()
    }

    override suspend fun onSurfaceDraw(holder: SurfaceHolder) = withContext(Dispatchers.IO) {
        countDownLatch.await()
        println("onSurfaceDraw period=$period")
        scheduleFlow(0, period).collect {
            draw(holder) {
                if (notDrawDataList.isEmpty()) {
                    cancelCircleDrawJob()
                    return@draw
                }
                it.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                drawBg(it)
                drawData(it)
            }
        }
    }

    // 画背景图片
    private fun drawBg(canvas: Canvas) {
        val bmp = bgBitmap ?: return
        if (!bmp.isRecycled) {
            canvas.drawBitmap(bmp, 0f, 0f, null)
        }
    }

    // 画心电数据
    private fun drawData(canvas: Canvas) {
        if (notDrawDataList.isNotEmpty()) {
            repeat(drawDataCountEachTime) {
                notDrawDataList.removeFirstOrNull()?.let {
                    drawDataList.add(it)
                }
            }
            // 最多只绘制 maxDataCount 个数据
            if (maxDataCount > 0 && drawDataList.size > maxDataCount) {
                repeat(drawDataList.size - maxDataCount) {
                    drawDataList.removeFirst()
                }
            }
        }
        if (drawDataList.isEmpty()) return

        dataPath.reset()
        var x = 0f
        dataPath.moveTo(x, drawDataList.first())
        (1 until drawDataList.size).forEach {
            x += stepX
            dataPath.lineTo(x, drawDataList[it])
        }
        dataPath.offset(0f, yOffset)
        canvas.drawPath(dataPath, dataPaint)
    }

    // 画水平线
    private fun drawHLine(canvas: Canvas) {
        val startX = 0f
        val stopX = width.toFloat()
        (0..hLineCount).forEach {
            if (it % 5 == 0) {
                gridPaint.pathEffect = null
                gridPaint.alpha = 255
            } else {
                gridPaint.pathEffect = dashPathEffect
                gridPaint.alpha = 100
            }
            val y = it * gridSpace.toFloat()
            canvas.drawLine(startX, y, stopX, y, gridPaint)
        }
    }

    // 画垂直线
    private fun drawVLine(canvas: Canvas) {
        val startY = 0f
        val stopY = height.toFloat()
        (0..vLineCount).forEach {
            if (it % 5 == 0) {
                gridPaint.pathEffect = null
                gridPaint.alpha = 255
            } else {
                gridPaint.pathEffect = dashPathEffect
                gridPaint.alpha = 100
            }
            val x = it * gridSpace.toFloat()
            canvas.drawLine(x, startY, x, stopY, gridPaint)
        }
    }

    companion object {
        private const val MM_PER_S = 25// 走速（速度）。默认为标准值：25mm/s
        private const val MM_PER_MV = 10// 增益（灵敏度）。默认为1倍：10mm/mV
    }
}

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    // 循环绘制任务
    private var circleDrawJob: Job? = null

    init {
        holder.addCallback(this)
        // 画布透明处理
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    /*
     下面的三个函数是 实现 SurfaceHolder.Callback 接口方法
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cancelCircleDrawJob()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    protected fun startCircleDrawJob() {
        if (circleDrawJob != null) return
        println("startCircleDrawJob")
        circleDrawJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            onSurfaceDraw(holder)
        }
    }

    protected fun cancelCircleDrawJob() {
        println("cancelCircleDrawJob")
        circleDrawJob?.cancel()
        circleDrawJob = null
    }

    protected fun draw(holder: SurfaceHolder, onDraw: (Canvas) -> Unit) {
        var canvas: Canvas? = null
        try {/*
            用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样就叫做双缓冲
            frontCanvas：实际显示的canvas。
            backCanvas：存储的是上一次更改前的canvas。
             */
            canvas = holder.lockCanvas() // 获取 backCanvas
            // 获取到的 Canvas 对象还是继续上次的 Canvas 对象，而不是一个新的 Canvas 对象。因此，之前的绘图操作都会被保留。
            // 在绘制前，通过 drawColor() 方法来进行清屏操作。
            canvas?.let {
                onDraw(it)
            }
        } finally {
            // 使用 backCanvas 替换 frontCanvas 作为新的 frontCanvas，原来的 frontCanvas 将切换到后台作为 backCanvas。
            try {
                holder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 绘制
     */
    abstract suspend fun onSurfaceDraw(holder: SurfaceHolder)

}
