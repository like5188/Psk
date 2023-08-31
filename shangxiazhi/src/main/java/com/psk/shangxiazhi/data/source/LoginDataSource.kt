package com.psk.shangxiazhi.data.source

import android.content.Context
import com.like.common.util.SPUtils
import com.szocet.pad1601pwd.DeviceUtils

class LoginDataSource {

    companion object {
        private const val SP_LOGIN = "sp_login"
    }

    fun getSerialNumber(context: Context): String {
        return DeviceUtils.getDeviceId(context)
    }

    fun login(serialNumber: String?, code: String?): Boolean {
        if (serialNumber.isNullOrEmpty()) {
            throw IllegalArgumentException("serialNumber is null or empty")
        }
        if (code.isNullOrEmpty()) {
            throw IllegalArgumentException("code is null or empty")
        }
        return DeviceUtils.calPassword(serialNumber) == code
    }

    fun isLogin(): Boolean {
        return SPUtils.getInstance().get(SP_LOGIN, false)
    }

    fun setLogin(isLogin: Boolean) {
        SPUtils.getInstance().put(SP_LOGIN, isLogin)
    }

}