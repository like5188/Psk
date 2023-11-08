package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.app.databinding.ActivityMainBinding
import com.psk.socket.SocketListener
import com.psk.socket.SocketServerService
import java.nio.ByteBuffer

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
                    println("包头 ${message.getShort()}")
                    println("设备编号 ${message.getInt()}")
                    println("采样率 ${message.getShort()}")
                    println("包采样周期数 ${message.get()}")
                    println("导联标识 ${message.get()}")
                    println("包序号(时间) ${message.getInt()}")
                    println("增益(float) ${message.getFloat()}")
                    val ecgData = ByteArray(240)
                    message.get(ecgData, 0, 240)
                    println("12导心电数据 ${ecgData.contentToString()}")
                    println("心率 ${message.getShort()}")
                    println("收缩压 ${message.getShort()}")
                    println("舒张压 ${message.getShort()}")
                    println("功率 ${message.getShort()}")
                    println("血氧 ${message.get()}")
                }
            })
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
    }
}