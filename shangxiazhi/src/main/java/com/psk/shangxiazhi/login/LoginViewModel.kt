package com.psk.shangxiazhi.login

import android.content.Context
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
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun getSerialNumber(context: Context): String {
        return shangXiaZhiBusinessRepository.getSerialNumber(context)
    }

    fun login(serialNumber: String?, code: String?, progressDialog: ProgressDialog) {
        viewModelScope.launch {
            DataHandler.collectWithProgress(progressDialog, block = {
                shangXiaZhiBusinessRepository.login(serialNumber, code)
            }, onError = { throwable ->
                _uiState.update {
                    it.copy(
                        isLogin = false, toastEvent = Event(ToastEvent(throwable = throwable))
                    )
                }
            }) { isLogin ->
                _uiState.update {
                    it.copy(
                        isLogin = isLogin, toastEvent = Event(ToastEvent(text = if (isLogin == true) "激活成功" else "激活失败"))
                    )
                }
            }
        }
    }

    fun setLogin(isLogin: Boolean) {
        shangXiaZhiBusinessRepository.setLogin(isLogin)
    }

}