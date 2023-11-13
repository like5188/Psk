package com.psk.shangxiazhi.data.source

import android.content.Context

class ShangXiaZhiBusinessRepository(
    private val loginDataSource: LoginDataSource,
) {
    fun getSerialNumber(context: Context): String {
        return loginDataSource.getSerialNumber(context)
    }

    fun login(serialNumber: String?, code: String?): Boolean {
        return true
        // todo
//        loginDataSource.saveCode(code)
//        return loginDataSource.login(serialNumber, code)
    }

    fun isLogin(context: Context): Boolean {
        return true
        // todo
//        val serialNumber = loginDataSource.getSerialNumber(context)
//        val code = loginDataSource.getCode()
//        return try {
//            loginDataSource.login(serialNumber, code)
//        } catch (e: Exception) {
//            false
//        }
    }

}