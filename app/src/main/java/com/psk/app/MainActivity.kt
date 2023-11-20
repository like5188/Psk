package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.showToast
import com.psk.app.databinding.ActivityMainBinding
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import com.psk.socket.SocketListener
import com.psk.socket.SocketServerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val repository = RepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnStart.setOnClickListener {
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
                    println("包头 ${message.short}")
                    println("设备编号 ${message.int}")
                    println("采样率 ${message.short}")
                    println("包采样周期数 ${message.get()}")
                    println("导联标识 ${message.get()}")
                    println("包序号(时间) ${message.int}")
                    println("增益(float) ${message.float}")
                    val ecgData = ByteArray(240)
                    message.get(ecgData, 0, 240)
                    println("12导心电数据 ${ecgData.contentToString()}")
                    println("心率 ${message.short}")
                    println("收缩压 ${message.short}")
                    println("舒张压 ${message.short}")
                    println("功率 ${message.short}")
                    println("血氧 ${message.get()}")
                    val coorYValues = ecgData.toList().chunked(12).map {
                        it.first().toFloat()
                    }.toFloatArray()
                    mBinding.ecgChartView.addData(coorYValues)
                }
            })
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
        mBinding.btnConnect.setOnClickListener {
            repository.init(this, "ER1 1391", "E7:B5:08:62:96:05")
            repository.connect(lifecycleScope, 0L, {
                showToast("心电仪连接成功，开始测量")
                job = lifecycleScope.launch {
                    repository.fetch().filterNotNull().map {
                        it.coorYValues
                    }.buffer(Int.MAX_VALUE).collect { coorYValues ->
                        println(coorYValues.size)
                        println(coorYValues.contentToString())
                        // 注意：此处不能使用 onEach 进行每个数据的延迟，因为只要延迟，由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
                        // 这也导致1秒钟时间段内，就算延迟1毫秒，实际上延迟却会达到10多毫秒，导致最多只能发射60多个数据（实际测试）。
                        // 这就导致远远达不到心电仪的采样率的100多，从而会导致数据堆积越来越多，导致界面看起来会延迟很严重。
                        coorYValues.toList().chunked(5).forEach {
                            // 5个一组，125多的采样率，那么1秒钟发射25组数据就好，平均每个数据需要延迟40毫秒。
                            delay(1)
                            mBinding.ecgChartView.addData(it.toFloatArray())
                        }
                    }
                }
            }) {
                showToast("心电仪连接失败，无法进行测量")
                job?.cancel()
                job = null
            }
        }
        mBinding.btnDisconnect.setOnClickListener {
            job?.cancel()
            job = null
            repository.close()
        }
        lifecycleScope.launch {
            RepositoryManager.init(this@MainActivity)
        }
    }
}