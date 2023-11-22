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
                    // iCV200A心电图仪模拟数据时每秒收到25次回调，每次回调包含12导心电数据为240 byte，所以某个导联的数据量为 240/12=20，所以采样率为25*20=500
                    print("包头 ${message.short}, ")
                    print("设备编号 ${message.int}, ")
                    print("采样率 ${message.short}, ")
                    print("包采样周期数 ${message.get()}, ")
                    print("导联标识 ${message.get()}, ")
                    print("包序号(时间) ${message.int}, ")
                    print("增益(float) ${message.float}, ")
                    val ecgData = ByteArray(240)
                    message.get(ecgData, 0, 240)
                    print("12导心电数据数量 ${ecgData.size}, ")
                    print("心率 ${message.short}, ")
                    print("收缩压 ${message.short}, ")
                    print("舒张压 ${message.short}, ")
                    print("功率 ${message.short}, ")
                    print("血氧 ${message.get()}, ")
                    // 这里的数据单位是 uV，需要 /1000f 转换成 mV
                    val coorYValues = ecgData.toList().chunked(12).map {
                        it.first() / 1000f * 300f
                    }
                    mBinding.ecgChartView.addData(coorYValues)
                    println()
                }
            })
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
        mBinding.btnConnect.setOnClickListener {
            repository.init(this, "A00213000334", "A0:02:13:00:03:34")
            repository.connect(lifecycleScope, 0L, {
                showToast("心电仪连接成功，开始测量")
                job = lifecycleScope.launch {
                    repository.fetch().filterNotNull().map {
                        it.coorYValues
                    }.buffer(Int.MAX_VALUE).collect {
                        mBinding.ecgChartView.addData(it.map {
                            -it// 取反，因为如果不处理，画出的波形图是反的
                        })
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
        mBinding.ecgChartView.init(125)
    }
}