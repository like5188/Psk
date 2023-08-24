package com.psk.shangxiazhi.login

import androidx.lifecycle.ViewModel
import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository

class LoginViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiRepository,
) : ViewModel() {

    suspend fun login(phone: String?, password: String?, type: Int?): LoginResult? {
        return shangXiaZhiRepository.login(phone, password, type)
    }

}