package com.psk.shangxiazhi.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository

class SplashViewModel(
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel() {

    fun isLogin(context: Context): Boolean {
        return shangXiaZhiBusinessRepository.isLogin(context)
    }
}
