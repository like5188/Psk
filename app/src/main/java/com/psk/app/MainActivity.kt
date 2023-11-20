package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.psk.app.databinding.ActivityMainBinding
import com.psk.socket.SocketListener
import com.psk.socket.SocketServerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

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
        lifecycleScope.launch {
            val coorYValues = (0 until 1000).map {
                Random.nextInt(1000) + Random.nextFloat()
            }
            // 模拟250采样率
//            coorYValues.chunked(250).forEach {
//                mBinding.ecgChartView1.addData(it.toFloatArray())
//                delay(1000)
//            }
            coorYValues.forEach {
                mBinding.ecgChartView.addData(floatArrayOf(it))
                delay(10)
            }
        }
    }
}