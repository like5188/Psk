package com.psk.shangxiazhi.train

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.DeviceManager
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.game.GameManagerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@OptIn(KoinApiExtension::class)
class TrainViewModel(deviceManager: DeviceManager) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(TrainUiState())
    val uiState = _uiState.asStateFlow()
    private val healthInfoRepository = deviceManager.healthInfoRepository

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

    fun saveHealthInfo(data: HealthInfo) {
        viewModelScope.launch {
            healthInfoRepository.save(data)
        }
    }

    companion object {
        private val TAG = TrainViewModel::class.java.simpleName
    }

}
