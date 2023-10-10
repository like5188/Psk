package com.psk.shangxiazhi.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.like.common.util.mvi.Event
import com.like.common.util.ToastEvent
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun getSerialNumber(context: Context): String {
        return shangXiaZhiBusinessRepository.getSerialNumber(context)
    }

    fun login(serialNumber: String?, code: String?) {
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