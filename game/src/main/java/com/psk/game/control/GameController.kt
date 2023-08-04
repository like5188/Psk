package com.psk.game.control

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.twsz.twsystempre.IGame
import com.twsz.twsystempre.UnityValueModel

class GameController(private val context: Context) {
    private var iGame: IGame? = null

    //创建一个ServiceConnection回调，通过IBinder进行交互
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected")
            //本句话借用：Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, "onServiceConnected")
            //下面是通过代理的方式将iBinder转成IMyAidlInterface
            iGame = IGame.Stub.asInterface(service)
        }
    }

    fun connectGameService() {
        //通过bindService启动服务
        Log.d(TAG, "bindService")
        val intent = Intent()
        intent.setAction("com.twsz.twsystempre.GameService.remoteBinder")
        intent.setPackage("com.twsz.twsystempre")
        context.bindService(intent, connection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    fun setUnityValueModel(unityValueModel: UnityValueModel?) {
        // 这里必须try，避免连接断开后调用抛异常
        try {
            iGame?.setUnityValueModel(unityValueModel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun destroy() {
        context.unbindService(connection)
    }

    companion object {
        private val TAG = GameController::class.java.simpleName
    }
}
