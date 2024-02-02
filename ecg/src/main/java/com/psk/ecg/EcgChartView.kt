package com.psk.ecg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import com.psk.ecg.effect.CirclePathEffect
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.DataPainter
import com.psk.ecg.painter.IBgPainter
import com.psk.ecg.painter.IDataPainter
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
    private var sampleRate = 0
    private var mm_per_s = 0
    private var mm_per_mv = 0
    private var gridSize = 0
    private var leadsCount = 0
    private var bgPainter: IBgPainter? = null
    private lateinit var dataPainters: List<IDataPainter>

    init {
        // 画布透明处理
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    /**
     * @param sampleRate        采样率
     * @param mm_per_s          走速（速度）。默认为标准值：25mm/s
     * @param mm_per_mv         增益（灵敏度）。默认为 1倍：10mm/mV
     * @param gridSize          一个小格子对应的像素。默认为设备实际 1mm对应的像素。
     * @param leadsCount        导联数量。默认为 1。
     * @param bgPainter         背景绘制者。默认为[BgPainter]。
     * 可以自己实现[IBgPainter]接口，或者自己创建[BgPainter]实例。
     * @param dataPainters      数据绘制者集合，有几个导联就需要几个绘制者。默认为包括[leadsCount]个[DataPainter]的集合.
     * 可以自己实现[IDataPainter]接口，或者自己创建[DataPainter]实例。
     */
    fun init(
        sampleRate: Int,
        mm_per_s: Int = 25,
        mm_per_mv: Int = 10,
        gridSize: Int = (context.resources.displayMetrics.densityDpi / 25.4f).toInt(),
        leadsCount: Int = 1,
        bgPainter: IBgPainter? = BgPainter(Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
            alpha = 120
        }, Paint().apply {
            color = Color.parseColor("#00a7ff")
            strokeWidth = 1f
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
            alpha = 90
        }, Paint().apply {
            color = Color.parseColor("#ffffff")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
            alpha = 125
        }),
        dataPainters: List<IDataPainter> = (0 until leadsCount).map {
            DataPainter(CirclePathEffect(), Paint().apply {
                color = Color.parseColor("#44C71E")
                strokeWidth = 3f
                style = Paint.Style.STROKE
                isAntiAlias = true
            })
        }
    ) {
        Log.w(TAG, "init")
        this.sampleRate = sampleRate
        this.mm_per_s = mm_per_s
        this.mm_per_mv = mm_per_mv
        this.gridSize = gridSize
        this.leadsCount = leadsCount
        this.bgPainter = bgPainter
        this.dataPainters = dataPainters
        calcParams()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.w(TAG, "onSizeChanged")
        calcParams()
    }

    // 计算相关参数
    private fun calcParams() {
        if (sampleRate <= 0 || mm_per_s <= 0 || mm_per_mv <= 0 || gridSize <= 0 || leadsCount <= 0 || width <= 0 || height <= 0 || !::dataPainters.isInitialized) {
            return
        }
        Log.i(
            TAG,
            "sampleRate=$sampleRate mm_per_s=$mm_per_s mm_per_mv=$mm_per_mv gridSize=$gridSize leadsCount=$leadsCount width=$width height=$height"
        )
        bgPainter?.init(width, height, gridSize, leadsCount)
        dataPainters.forEachIndexed { index, dataPainter ->
            dataPainter.init(
                mm_per_s,
                mm_per_mv,
                getPeriod(),
                sampleRate,
                width,
                height,
                gridSize,
                leadsCount,
                index,
                bgPainter?.hasStandardSquareWave() == true
            )
        }
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
     * @param list  需要添加的数据，每个导联数据都是List。mV。
     */
    fun addData(list: List<List<Float>>) {
        if (list.size != leadsCount) {
            Log.e(TAG, "添加数据失败，和初始化时传入的导联数不一致！")
            return
        }
        // 在surface创建后，即可以绘制的时候，才允许添加数据，在surface销毁后禁止添加数据，以免造成数据堆积。
        if (!isSurfaceCreated) {
            Log.e(TAG, "添加数据失败，isSurfaceCreated is false")
            return
        }
        if (!::dataPainters.isInitialized) {
            Log.e(TAG, "添加数据失败，请先调用 init 方法进行初始化")
            return
        }
        dataPainters.forEachIndexed { index, dataPainter ->
            dataPainter.addData(list[index])
        }
        startJob()// 有数据时启动任务
    }

    override fun onDrawFrame(canvas: Canvas) {
        if (dataPainters.all { !it.hasNotDrawData() }) {
            doDraw(canvas)// 这里绘制一次最近的数据，避免前后台切换后由于没有数据传递过来而不进行绘制，造成界面空白。
            cancelJob("没有数据")// 没有数据时取消任务
            return
        }
        doDraw(canvas)
    }

    /**
     * 获取当前绘制的位图。用于保存图片。
     * 因为 SurfaceView 不能像普通 view 那样使用 view.draw(canvas) 来获取内容。
     */
    fun getBitmap(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        doDraw(Canvas(this))
    }

    private fun doDraw(canvas: Canvas) {
        Log.v(TAG, "doDraw")
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        bgPainter?.draw(canvas)
        dataPainters.forEach {
            it.draw(canvas)
        }
    }

    companion object {
        private val TAG = EcgChartView::class.java.simpleName
    }
}
