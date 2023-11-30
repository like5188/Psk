package com.psk.shangxiazhi.data.source

import android.content.Context
import com.psk.shangxiazhi.data.source.local.LoginDataSource

class ShangXiaZhiBusinessRepository(
    private val loginDataSource: LoginDataSource,
) {
    fun getSerialNumber(context: Context): String {
        return loginDataSource.getSerialNumber(context)
    }

    fun login(serialNumber: String?, code: String?): Boolean {
        loginDataSource.saveCode(code)
        return loginDataSource.login(serialNumber, code)
    }

    fun isLogin(context: Context): Boolean {
        val serialNumber = loginDataSource.getSerialNumber(context)
        val code = loginDataSource.getCode()
        return try {
            loginDataSource.login(serialNumber, code)
        } catch (e: Exception) {
            false
        }
    }

}