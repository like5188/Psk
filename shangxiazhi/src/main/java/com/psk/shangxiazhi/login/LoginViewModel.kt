package com.psk.shangxiazhi.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(phone: String?, password: String?, type: Int?, progressDialog: ProgressDialog) {
        viewModelScope.launch {
            DataHandler.collectWithProgress(progressDialog, block = {
                shangXiaZhiRepository.login(phone, password, type)
            }, onError = { throwable ->
                _uiState.update {
                    it.copy(
                        login = null, toastEvent = Event(ToastEvent(throwable = throwable))
                    )
                }
            }) { loginResult ->
                if (loginResult?.code == 0) {
                    _uiState.update {
                        it.copy(
                            login = loginResult.login, toastEvent = Event(ToastEvent(text = "登录成功"))
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            login = null, toastEvent = Event(ToastEvent(text = loginResult?.msg ?: "登录失败"))
                        )
                    }
                }
            }
        }
    }

}