package com.psk.shangxiazhi.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.ToastEvent
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    fun logout(progressDialog: ProgressDialog) {
        val login = Login.getCache()
        if (login == null) {
            _uiState.update {
                it.copy(
                    logout = false,
                    toastEvent = Event(ToastEvent(text = "获取token失败"))
                )
            }
            return
        }
        viewModelScope.launch {
            DataHandler.collectWithProgress(progressDialog, block = {
                shangXiaZhiRepository.logout(login.patient_token)
            }, onError = { throwable ->
                _uiState.update {
                    it.copy(
                        logout = false,
                        toastEvent = Event(ToastEvent(throwable = throwable))
                    )
                }
            }) {
                _uiState.update {
                    it.copy(
                        logout = true,
                        toastEvent = Event(ToastEvent(text = "退出登录成功"))
                    )
                }
            }
        }
    }

}
