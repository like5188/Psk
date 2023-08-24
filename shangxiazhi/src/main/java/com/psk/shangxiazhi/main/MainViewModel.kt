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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.like.common.util.SPUtils
import com.like.common.util.mvi.Event
import com.psk.common.util.DataHandler
import com.psk.common.util.SecondCountDownTimer
import com.psk.common.util.ToastEvent
import com.psk.common.util.showToast
import com.psk.device.DeviceType
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.devices.SelectDeviceDialogFragment
import com.psk.shangxiazhi.game.GameManagerService
import com.psk.shangxiazhi.scene.SceneActivity
import com.psk.shangxiazhi.util.SP_LOGIN
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(KoinApiExtension::class)
class MainViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiRepository,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    private lateinit var activity: ComponentActivity
    private var gameManagerService: GameManagerService? = null
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd HH:mm:ss"))
    private val gson: Gson by inject()
    private val countDownTimer by lazy {
        object : SecondCountDownTimer(Int.MAX_VALUE.toLong(), 1) {
            override fun onSecondsTick(secondsUntilFinished: Long) {
                _uiState.update {
                    it.copy(
                        time = sdf.format(Date())
                    )
                }
            }

            override fun onFinish() {
            }
        }
    }

    init {
        countDownTimer.start()
    }

    suspend fun getUser(context: Context) {
        val loginJsonString = SPUtils.getInstance().get<String?>(SP_LOGIN, null)
        if (loginJsonString.isNullOrEmpty()) {
            _uiState.update {
                it.copy(toastEvent = Event(ToastEvent(text = "获取token失败")))
            }
            return
        }
        val login = try {
            gson.fromJson<Login?>(loginJsonString, object : TypeToken<Login>() {}.type)
        } catch (e: Exception) {
            null
        }
        if (login == null) {
            _uiState.update {
                it.copy(toastEvent = Event(ToastEvent(text = "获取token失败")))
            }
            return
        }

        DataHandler.collect(context, block = {
            shangXiaZhiRepository.getUser(login.patient_token)
        }, onError = {
            _uiState.update {
                it.copy(toastEvent = Event(ToastEvent(text = "获取用户信息失败")))
            }
        }) { getUserResult ->
            if (getUserResult?.code == 0) {
                _uiState.update {
                    it.copy(
                        userName = getUserResult.user.name
                    )
                }
            } else {
                _uiState.update {
                    it.copy(toastEvent = Event(ToastEvent(text = "获取用户信息失败")))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer.cancel()
    }

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
                    if (!it.containsKey(DeviceType.ShangXiaZhi)) {
                        activity.showToast("请先选择上下肢设备")
                    } else {
                        gameManagerService?.start(it, scene, resistanceInt = 1, passiveModule = true, timeInt = 2)
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
