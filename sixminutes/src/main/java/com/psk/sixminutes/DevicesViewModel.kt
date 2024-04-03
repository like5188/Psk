package com.psk.sixminutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class DevicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState = _uiState.asStateFlow()
    private val sdf: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE")
    }

    init {
        viewModelScope.launch {
            scheduleFlow(0, 1000).collect {
                _uiState.update {
                    it.copy(
                        date = sdf.format(Date())
                    )
                }
            }
        }
    }

}
