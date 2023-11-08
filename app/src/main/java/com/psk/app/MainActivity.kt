package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.app.databinding.ActivityMainBinding
import com.psk.socket.SocketServerService

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnStart.setOnClickListener {
            SocketServerService.start(this, 7777)
        }
        mBinding.btnStop.setOnClickListener {
            SocketServerService.stop(this)
        }
    }
}