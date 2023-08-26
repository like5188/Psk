package com.psk.shangxiazhi.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiBusinessRepository,
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
            }) { login ->
                _uiState.update {
                    it.copy(
                        login = login, toastEvent = Event(ToastEvent(text = "登录成功"))
                    )
                }
            }
        }
    }

}