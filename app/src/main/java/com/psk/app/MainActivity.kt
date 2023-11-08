package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.app.databinding.ActivityMainBinding
import com.psk.socket.Msg
import com.psk.socket.SocketListener
import com.psk.socket.SocketServerService

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnStart.setOnClickListener {
            SocketServerService.start(this, 7777, object : SocketListener {
                override fun onConnected() {
                    println("onConnected")
                }

                override fun onDisConnected() {
                    println("onDisConnected")
                }

                override fun onReceived(msg: Msg?) {
                    println("onReceived $msg")
                }

            })
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
    }
}