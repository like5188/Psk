package com.psk.shangxiazhi.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.SecondsTimer
import com.psk.device.RepositoryManager
import com.psk.device.ScanManager
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(KoinApiExtension::class)
class MainViewModel(
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd HH:mm:ss"))
    private val secondsTimer by lazy {
        SecondsTimer().apply {
            onTick = {
                _uiState.update {
                    it.copy(
                        time = sdf.format(Date())
                    )
                }
            }
        }
    }

    init {
        secondsTimer.start()
    }

    fun init(activity: ComponentActivity) {
        viewModelScope.launch {
            ScanManager.init(activity)
            RepositoryManager.init(activity)
        }
    }

    fun isLogin(context: Context): Boolean {
        return shangXiaZhiBusinessRepository.isLogin(context)
    }

    override fun onCleared() {
        super.onCleared()
        secondsTimer.stop()
    }

}
