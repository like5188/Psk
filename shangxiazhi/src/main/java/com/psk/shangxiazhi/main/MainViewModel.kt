package com.psk.shangxiazhi.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.like.common.util.mvi.Event
import com.psk.common.util.DataHandler
import com.psk.common.util.SecondCountDownTimer
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository
import com.psk.shangxiazhi.game.GameManagerService
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
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd HH:mm:ss"))
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

    //创建一个ServiceConnection回调，通过IBinder进行交互
    private val localConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected")
            //本句话借用：Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, "onServiceConnected")
            val localBinder = service as? GameManagerService.LocalBinder
            _uiState.update {
                it.copy(gameManagerService = localBinder?.getService())
            }
        }
    }

    init {
        countDownTimer.start()
    }

    suspend fun getUser(context: Context) {
        val login = Login.getCache()
        if (login == null) {
            _uiState.update {
                it.copy(toastEvent = Event(ToastEvent(text = "获取token失败")))
            }
            return
        }

        DataHandler.collect(context, block = {
            shangXiaZhiRepository.getUser(login.patient_token)
        }, onError = { throwable ->
            _uiState.update {
                it.copy(toastEvent = Event(ToastEvent(throwable = throwable)))
            }
        }) { user ->
            _uiState.update {
                it.copy(
                    userName = user?.name ?: ""
                )
            }
        }
    }

    fun bindGameManagerService(context: Context) {
        Log.w(TAG, "bindGameManagerService")
        val intent = Intent(context, GameManagerService::class.java)
        context.bindService(intent, localConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    fun unbindGameManagerService(context: Context) {
        try {
            Log.w(TAG, "unbindGameManagerService")
            context.unbindService(localConnection)
        } catch (e: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer.cancel()
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

}
