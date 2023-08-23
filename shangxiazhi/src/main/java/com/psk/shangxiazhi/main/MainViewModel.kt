package com.psk.shangxiazhi.main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.psk.common.util.showToast
import com.psk.device.DeviceType
import com.psk.shangxiazhi.devices.SelectDeviceDialogFragment
import com.psk.shangxiazhi.game.GameManagerService
import com.psk.shangxiazhi.scene.SceneActivity
import com.twsz.twsystempre.TrainScene

class MainViewModel : ViewModel() {
    private lateinit var activity: ComponentActivity
    private var gameManagerService: GameManagerService? = null

    //创建一个ServiceConnection回调，通过IBinder进行交互
    private val localConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected")
            //本句话借用：Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, "onServiceConnected")
            val localBinder = service as? GameManagerService.LocalBinder
            gameManagerService = localBinder?.getService()
            gameManagerService?.initBle(activity)
        }
    }

    fun bindGameManagerService(activity: ComponentActivity) {
        this.activity = activity
        //通过bindService启动服务
        Log.w(TAG, "bindGameManagerService")
        val intent = Intent(activity, GameManagerService::class.java)
        activity.bindService(intent, localConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    fun unbindGameManagerService(context: Context) {
        try {
            Log.w(TAG, "unbindGameManagerService")
            context.unbindService(localConnection)
            gameManagerService = null
        } catch (e: Exception) {
        }
    }

    /**
     * 选择场景、设备，并启动游戏app
     */
    fun selectSceneAndDeviceAndStartGame(activity: FragmentActivity) {
        SceneActivity.start(activity) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@start
            }
            val scene = it.data?.getSerializableExtra(SceneActivity.KEY_DATA) as? TrainScene ?: return@start
            SelectDeviceDialogFragment.newInstance(
                arrayOf(
                    DeviceType.ShangXiaZhi,
                    DeviceType.BloodOxygen,
                    DeviceType.BloodPressure,
                    DeviceType.HeartRate,
                )
            ).apply {
                onSelected = {
                    val shangXiaZhiDeviceAddress = it.getOrDefault(DeviceType.ShangXiaZhi, null)?.address ?: ""
                    val heartRateDeviceAddress = it.getOrDefault(DeviceType.HeartRate, null)?.address ?: ""
                    val bloodOxygenDeviceAddress = it.getOrDefault(DeviceType.BloodOxygen, null)?.address ?: ""
                    val bloodPressureDeviceAddress = it.getOrDefault(DeviceType.BloodPressure, null)?.address ?: ""
                    if (shangXiaZhiDeviceAddress.isEmpty()) {
                        activity.showToast("请先选择上下肢设备")
                    } else {
                        gameManagerService?.start(
                            shangXiaZhiDeviceAddress,
                            heartRateDeviceAddress,
                            bloodOxygenDeviceAddress,
                            bloodPressureDeviceAddress,
                            scene,
                            resistanceInt = 1,
                            passiveModule = true,
                            timeInt = 2
                        )
                    }
                }
                show(activity)
            }
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

}
