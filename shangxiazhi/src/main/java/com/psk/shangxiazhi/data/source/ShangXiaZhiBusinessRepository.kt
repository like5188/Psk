package com.psk.shangxiazhi.data.source

import android.content.Context

class ShangXiaZhiBusinessRepository(
    private val loginDataSource: LoginDataSource,
) {
    fun getSerialNumber(context: Context): String {
        return loginDataSource.getSerialNumber(context)
    }

    fun login(serialNumber: String?, code: String?): Boolean {
        return loginDataSource.login(serialNumber, code)
    }

    fun isLogin(): Boolean {
        return loginDataSource.isLogin()
    }

    fun setLogin(isLogin: Boolean) {
        loginDataSource.setLogin(isLogin)
    }

}