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
import com.like.common.util.PhoneUtils
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
            color = Color.parseColor("#021F52")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    // 一下三个属性按照心电设备参数设置
    private val mm_Per_s = 25// 走速（速度）。标准值：25mm/s
    private val mm_Per_mV = 10// 增益（灵敏度）。1倍：10mm/mV
    private val sampleRate = 125// 采样率

    private val recommendInterval = 30.0// 建议循环间隔时间
    private val interval = 1000 / sampleRate// 绘制每个数据的间隔时间

    // 每次绘制的数据量。避免数据太多，1秒钟绘制不完，造成界面延迟严重。
    // 因为 scheduleFlow 循环任务在间隔时间太短或者处理业务耗时太长时会造成误差太多。
    // 经测试，大概16毫秒以上循环误差就比较小了，建议使用30毫秒以上，这样绘制效果较好。
    // Math.ceil()向上取整
    private val drawDataCountEachTime = if (interval < recommendInterval) Math.ceil(recommendInterval / interval).toInt() else interval

    private var gridSpace = 0// 一个小格子对应的像素，即1mm对应的像素。px/mm
    private var hLineCount = 0// 水平线的数量
    private var vLineCount = 0// 垂直线的数量
    private var yOffset = 0f// y轴偏移。因为原始的x轴在视图顶部。所以需要把x轴移动到视图垂直中心位置
    private var stepX = 0f// x方向的步进，两个数据在x轴方向的距离。px
    private var maxDataCount = 0// 能显示的最大数据量
    private val dashPathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)// 虚线
    private var bgBitmap: Bitmap? = null// 背景图片
    private val notDrawData = mutableListOf<Float>()// 未绘制的数据
    private val drawData = mutableListOf<Float>()// 需要绘制的数据
    private val dataPath = Path()

    init {
        // 1mm对应的像素值
        gridSpace = (PhoneUtils.getDensityDpi(context) / 25.4f).toInt()
        stepX = gridSpace * mm_Per_s / sampleRate.toFloat()
        println("gridSpace=$gridSpace stepX=$stepX")
    }

    /**
     * 添加 uV 数据。
     */
    fun addUvData(data: List<Float>) {
        notDrawData.addAll(data.map {
            // 把uV电压值转换成y轴坐标值
            val mV = it / 1000// uV转换成mV
            val mm = mV * mm_Per_mV// mV转mm
            mm * gridSpace// mm转px
        })
    }

    /**
     * 添加 mV 数据。
     */
    fun addMvData(data: List<Float>) {
        notDrawData.addAll(data.map {
            // 把uV电压值转换成y轴坐标值
            val mm = it * mm_Per_mV// mV转mm
            mm * gridSpace// mm转px
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        hLineCount = h / gridSpace
        vLineCount = w / gridSpace
        val axisXCount = (hLineCount - hLineCount % 5) / 2
        yOffset = axisXCount * gridSpace.toFloat()
        maxDataCount = (w / stepX).toInt()
        // 绘制背景到bitmap中
        bgBitmap?.recycle()
        bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            drawHLine(canvas)
            drawVLine(canvas)
        }
        println("onSizeChanged w=$w h=$h hLineCount=$hLineCount vLineCount=$vLineCount axisXCount=$axisXCount yOffset=$yOffset maxDataCount=$maxDataCount")
    }

    override suspend fun onSurfaceDraw(holder: SurfaceHolder) {
        val period = 1000L / sampleRate * drawDataCountEachTime
        scheduleFlow(0, period).collect {
            draw(holder) {
                drawBg(it)
                drawData(it)
            }
        }
    }

    // 画背景图片
    private fun drawBg(canvas: Canvas) {
        bgBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    // 画心电数据
    private fun drawData(canvas: Canvas) {
        repeat(drawDataCountEachTime) {
            notDrawData.removeFirstOrNull()?.let {
                drawData.add(it)
            }
        }
        // 最多只绘制 maxDataCount 个数据
        if (maxDataCount > 0 && drawData.size > maxDataCount) {
            repeat(drawData.size - maxDataCount) {
                drawData.removeFirst()
            }
        }
        if (drawData.isEmpty()) return
        dataPath.reset()
        var x = 0f
        dataPath.moveTo(x, drawData.first())
        drawData.forEach {
            x += stepX
            dataPath.lineTo(x, it + yOffset)
        }
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

}

abstract class AbstractSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private var scheduleJob: Job? = null

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
        scheduleJob?.cancel()
        scheduleJob = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        scheduleJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Default) {
            onSurfaceDraw(holder)
        }
    }

    protected fun draw(holder: SurfaceHolder, onDraw: (Canvas) -> Unit) {
        var canvas: Canvas? = null
        try {
            /*
            用了两个画布，一个进行临时的绘图，一个进行最终的绘图，这样就叫做双缓冲
            frontCanvas：实际显示的canvas。
            backCanvas：存储的是上一次更改前的canvas。
             */
            canvas = holder.lockCanvas() // 获取 backCanvas
            // 获取到的 Canvas 对象还是继续上次的 Canvas 对象，而不是一个新的 Canvas 对象。因此，之前的绘图操作都会被保留。
            // 在绘制前，通过 drawColor() 方法来进行清屏操作。
            canvas?.let {
                it.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
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