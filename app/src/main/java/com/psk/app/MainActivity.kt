package com.psk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.app.databinding.ActivityMainBinding
import com.psk.socket.SocketServer
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnStart.setOnClickListener {
            thread {
                val port = 7777
                val server: WebSocketServer = SocketServer(InetSocketAddress(port))
                server.run()
            }
        }
        mBinding.btnStop.setOnClickListener {
        }
    }
}