package com.psk.shangxiazhi.splash

import androidx.lifecycle.ViewModel
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository

class SplashViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiBusinessRepository,
) : ViewModel() {

    fun isLogin(): Boolean {
        return shangXiaZhiRepository.isLogin()
    }
}
