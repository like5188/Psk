package com.psk.shangxiazhi.data.source

import com.like.common.util.SPUtils

class ShangXiaZhiBusinessRepository(
    private val loginDataSource: LoginDataSource,
) {
    companion object {
        private const val SP_LOGIN = "sp_login"
    }

    suspend fun login(uuid: String?, code: String?): Boolean {
        return loginDataSource.load(uuid, code)
    }

    fun isLogin(): Boolean {
        return SPUtils.getInstance().get(SP_LOGIN, false)
    }

    fun setLogin(isLogin: Boolean) {
        SPUtils.getInstance().put(SP_LOGIN, isLogin)
    }

}