package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.dp
import com.like.common.util.showToast
import com.psk.app.databinding.ActivityMainBinding
import com.psk.device.DeviceRepositoryManager
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
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnStart.setOnClickListener {
            mBinding.ecgChartView1.init(250)
            mBinding.ecgChartView2.init(250)
            mBinding.ecgChartView3.init(250)
            mBinding.ecgChartViewAvr.init(250)
            mBinding.ecgChartViewAvl.init(250)
            mBinding.ecgChartViewAvf.init(250)
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
                        message.short
                    }
                    print("12导心电数据 ${ecgData}, ")
                    print("心率 ${message.short}, ")
                    print("收缩压 ${message.short}, ")
                    print("舒张压 ${message.short}, ")
                    print("功率 ${message.short}, ")
                    print("血氧 ${message.get()}, ")

                    val list = ecgData.chunked(12)
                    mBinding.ecgChartView1.addData(
                        list.map { it.first() / -1000f }// 这里的数据单位是 uV，需要 /1000f 转换成 mV
                    )
                    mBinding.ecgChartView2.addData(
                        list.map { it[1] / -1000f }
                    )
                    mBinding.ecgChartView3.addData(
                        list.map { it[2] / -1000f }
                    )
                    mBinding.ecgChartViewAvr.addData(
                        list.map { it[3] / -1000f }
                    )
                    mBinding.ecgChartViewAvl.addData(
                        list.map { it[4] / -1000f }
                    )
                    mBinding.ecgChartViewAvf.addData(
                        list.map { it[5] / -1000f }
                    )
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
            mBinding.ecgChartView1.init(250, 10.dp)
            repository.init(this, "C00228000695", "C0:02:28:00:06:95")
            lifecycleScope.launch {
                PermissionUtils.requestConnectEnvironment(this@MainActivity)
                repository.connect(lifecycleScope, 0L, {
                    showToast("心电仪连接成功，开始测量")
                    job = lifecycleScope.launch {
                        repository.fetch().filterNotNull().map {
                            it.coorYValues
                        }.buffer(Int.MAX_VALUE).collect {
                            mBinding.ecgChartView1.addData(it.map {
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
        }
        mBinding.btnDisconnect.setOnClickListener {
            job?.cancel()
            job = null
            repository.close()
        }
        lifecycleScope.launch {
            DeviceRepositoryManager.init(this@MainActivity)
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