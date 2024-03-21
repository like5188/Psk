package com.psk.shangxiazhi.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // 使用 MutableState 表示 TextField 状态：避免使用 StateFlow 等响应式流表示 TextField 状态，因为这些结构可能会引入异步延迟。
    var code by mutableStateOf("")
        private set

    fun updateCode(input: String) {
        code = input
    }

   suspend fun getSerialNumber(context: Context) {
        val serialNumber = shangXiaZhiBusinessRepository.getSerialNumber(context)
        _uiState.update {
            it.copy(
                serialNumber = serialNumber
            )
        }
    }

    suspend fun login(serialNumber: String?, code: String?) {
        try {
            val isLogin = shangXiaZhiBusinessRepository.login(serialNumber, code)
            _uiState.update {
                it.copy(
                    isLogin = isLogin, toastEvent = Event(ToastEvent(text = if (isLogin) "激活成功" else "激活失败"))
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLogin = false, toastEvent = Event(ToastEvent(throwable = e))
                )
            }
        }
    }

}