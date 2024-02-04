package com.psk.app

import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.dp
import com.like.common.util.showToast
import com.psk.app.databinding.ActivityMainBinding
import com.psk.app.pdf.PdfActivity
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import com.psk.ecg.effect.CirclePathEffect
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.DynamicDataPainter
import com.psk.socket.SocketListener
import com.psk.socket.SocketServerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.tv.text = "25 mm/s    10mm/mV"
        mBinding.btnPdf.setOnClickListener {
            startActivity(Intent(this, PdfActivity::class.java))
        }
        mBinding.btn1.setOnClickListener {
            mBinding.ecgChartView.setMmPerMv(1)
        }
        mBinding.btnStart.setOnClickListener {
            // E/SocketServerService: Parameter specified as non-null is null: method kotlin.jvm.internal.Intrinsics.checkNotNullParameter, parameter conn
            SocketServerService.start(this, 7777, object : SocketListener {
                override fun onOpen(address: String?) {
                    println("onOpen address=$address")
                }

                override fun onClose(code: Int, reason: String) {
                    println("onClose code=$code reason=$reason")
                }

                override fun onError(e: Exception) {
                    println("onError ${e.message}")
                }

                override fun onMessage(message: ByteBuffer) {
                    // iCV200A心电图仪模拟数据时每秒收到25次回调，每次回调包含12导心电数据为240 byte，所以某个导联的数据量为 240/12/2(2个字节一个数据)=10，所以采样率为25*10=250
                    println("数据量 ${message.capacity()}, ")
                    print("包头 ${message.short}, ")
                    print("设备编号 ${message.int}, ")
                    print("采样率 ${message.short}, ")
                    print("包采样周期数 ${message.get()}, ")
                    print("导联标识 ${message.get()}, ")
                    print("包序号(时间) ${message.int}, ")
                    print("增益(float) ${message.float}, ")
                    val ecgData = (0 until 120).map {
                        message.short / -1000f
                    }
                    print("12导心电数据 ${ecgData}, ")
                    print("心率 ${message.short}, ")
                    print("收缩压 ${message.short}, ")
                    print("舒张压 ${message.short}, ")
                    print("功率 ${message.short}, ")
                    print("血氧 ${message.get()}, ")

                    val datas = listOf(
                        mutableListOf<Float>(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                    )
                    ecgData.forEachIndexed { index, data ->
                        datas[index % 12].add(data)
                    }
                    mBinding.ecgChartView.addData(datas)
                    println()
                }
            })
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
        mBinding.btnConnect.setOnClickListener {
//            mBinding.ecgChartView.init(125, 10.dp)
//            repository.init(this, "A00219000219", "A0:02:19:00:02:19")
//            mBinding.ecgChartView.init(128, 10.dp)
//            repository.init(this, "ER1 0514", "CB:5D:19:C4:C3:A5")
            repository.init(this, "C00228000695", "C0:02:28:00:06:95")
            lifecycleScope.launch {
                PermissionUtils.requestConnectEnvironment(this@MainActivity)
                repository.connect(lifecycleScope, 0L, {
                    showToast("心电仪连接成功，开始测量")
                    job = lifecycleScope.launch {
                        repository.fetch().filterNotNull().map {
                            it.coorYValues
                        }.buffer(Int.MAX_VALUE).collect {
                            mBinding.ecgChartView.addData(listOf(it.map { -it }))// 取反，因为如果不处理，画出的波形图是反的
                        }
                    }
                }) {
                    showToast("心电仪连接失败，无法进行测量")
                    job?.cancel()
                    job = null
                }
            }
        }
        mBinding.btnDisconnect.setOnClickListener {
            job?.cancel()
            job = null
            repository.close()
        }
        lifecycleScope.launch {
            DeviceRepositoryManager.init(this@MainActivity)
        }

        mBinding.ecgChartView.apply {
            setSampleRate(250)
            setGridSize(10f.dp)
            setBgPainter(BgPainter(Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 2f
                isAntiAlias = true
                alpha = 120
            }, Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 1f
                isAntiAlias = true
                pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
                alpha = 90
            }, Paint().apply {
                color = Color.parseColor("#000000")
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
                alpha = 125
            }))
            setDataPainters((0 until 12).map {
                DynamicDataPainter(CirclePathEffect(), Paint().apply {
                    color = Color.parseColor("#44C71E")
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                })
            })
        }
    }

    override fun onResume() {
        super.onResume()
        println("MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        println("MainActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("MainActivity onDestroy")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        println("MainActivity onBackPressed")
        SocketServerService.stop(this)
        job?.cancel()
        job = null
        try {
            repository.close()
        } catch (e: Exception) {
        }
        finish()
    }

}