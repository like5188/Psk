package com.psk.shangxiazhi.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.common.util.scheduleFlow
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

    init {
        viewModelScope.launch {
            scheduleFlow(0, 1000).collect {
                _uiState.update {
                    it.copy(
                        time = sdf.format(Date())
                    )
                }
            }
        }
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

}
