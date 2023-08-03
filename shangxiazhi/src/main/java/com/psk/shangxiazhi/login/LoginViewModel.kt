package com.psk.shangxiazhi.login

import androidx.lifecycle.ViewModel
import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.data.source.LoginRepository

class LoginViewModel(
    private val loginRepository: LoginRepository,
) : ViewModel() {

    suspend fun login(phone: String?, password: String?, type: Int?): LoginResult? {
        return loginRepository.login(phone, password, type)
    }

}