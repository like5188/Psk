package com.psk.ecg.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.IBgPainter
import com.psk.ecg.painter.IDataPainter
import com.psk.ecg.painter.IDynamicDataPainter
import com.psk.ecg.util.TAG

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
    1倍电压：1mV=10mm；1/2电压：1mV=5mm；2倍电压：1mV=20mm
    注意：如果采用非1倍电压，在计算结果时需要还原。
 */
abstract class BaseEcgView(context: Context, attrs: AttributeSet?) : BaseSurfaceView(context, attrs) {
    private var mm_per_s = 0
    private var mm_per_mv = 0
    private var gridSize = 0f
    private var bgPainter: IBgPainter? = null
    protected var sampleRate = 0
        private set
    protected lateinit var dataPainters: List<IDataPainter>
        private set
    protected var leadsCount = 0
        private set

    init {
        // 画布透明处理
        this.setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    /**
     * @param sampleRate        采样率。
     * @param bgPainter         背景绘制者。库中默认实现为[BgPainter]。[BgPainter]。
     * 可以自己实现[IBgPainter]接口，或者自己创建[BgPainter]实例。
     * @param dataPainters      数据绘制者集合，有几个导联就需要几个绘制者。库中默认实现为[PeriodicDataPainter]、[OnceDataPainter]
     * 可以自己实现[IDynamicDataPainter]或者[IOnceDataPainter]接口，或者自己创建[PeriodicDataPainter]或者[OnceDataPainter]实例。
     * @param mm_per_s          走速（速度）。默认为标准值：25mm/s
     * @param mm_per_mv         增益（灵敏度）。默认为 1倍：10mm/mV
     * @param gridSize          一个小格子对应的像素值。默认为设备屏幕上1mm对应的像素，即一个小格子为1mm。
     */
    @JvmOverloads
    fun init(
        sampleRate: Int,
        bgPainter: IBgPainter?,
        dataPainters: List<IDataPainter>,
        mm_per_s: Int = 25,
        mm_per_mv: Int = 10,
        gridSize: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, context.resources.displayMetrics)
    ) {
        Log.w(TAG, "init")
        this.sampleRate = sampleRate
        this.bgPainter = bgPainter
        this.dataPainters = dataPainters
        this.mm_per_s = mm_per_s
        this.mm_per_mv = mm_per_mv
        this.gridSize = gridSize
        this.leadsCount = dataPainters.size
        calcParams()
    }

    protected fun initialized() = ::dataPainters.isInitialized

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.w(TAG, "onSizeChanged")
        calcParams()
    }

    // 计算相关参数
    private fun calcParams() {
        if (sampleRate <= 0 || mm_per_s <= 0 || mm_per_mv <= 0 || gridSize <= 0f || leadsCount <= 0 || width <= 0 || height <= 0) {
            return
        }
        Log.i(
            TAG,
            "calcParams sampleRate=$sampleRate mm_per_s=$mm_per_s mm_per_mv=$mm_per_mv gridSize=$gridSize leadsCount=$leadsCount width=$width height=$height"
        )
        bgPainter?.init(width, height, gridSize, leadsCount)
        repeat(leadsCount) { leadsIndex ->
            // 根据采样率计算
            val stepX = gridSize * mm_per_s / sampleRate
            // 一个导联的高度
            val leadsH = height.toFloat() / leadsCount
            val yOffset = leadsH / 2 + leadsIndex * leadsH// x坐标轴移动到中间
            val xOffset = if (bgPainter?.hasStandardSquareWave() == true) {// 是否绘制标准方波
                gridSize * 15// 3个大格
            } else {
                0f
            }
            val maxShowNumbers = ((width - xOffset) / stepX).toInt()
            Log.i(TAG, "第 ${leadsIndex + 1} 导联：stepX=$stepX xOffset=$xOffset yOffset=$yOffset maxShowNumbers=$maxShowNumbers")
            onInitData(leadsIndex, mm_per_mv, sampleRate, gridSize, stepX, xOffset, yOffset, maxShowNumbers)
        }
    }

    protected fun doDraw(canvas: Canvas) {
        if (!initialized()) {
            return
        }
        Log.v(TAG, "doDraw")
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        bgPainter?.draw(canvas)
        dataPainters.forEachIndexed { index, dataPainter ->
            Log.v(TAG, "第 ${index + 1} 导联：draw")
            dataPainter.draw(canvas)
        }
    }

    /**
     * 获取当前绘制的位图。用于保存图片。
     * 因为 SurfaceView 不能像普通 view 那样使用 view.draw(canvas) 来获取内容。
     */
    fun getBitmap(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        doDraw(Canvas(this))
    }

    /**
     * 初始化导联数据
     * @param leadsIndex                导联索引，从0开始。
     */
    protected abstract fun onInitData(
        leadsIndex: Int,
        mm_per_mv: Int,
        sampleRate: Int,
        gridSize: Float,
        stepX: Float,
        xOffset: Float,
        yOffset: Float,
        maxShowNumbers: Int
    )

}
