package com.psk.shangxiazhi.data.source

import android.content.Context
import com.like.common.util.SPUtils
import com.szocet.pad1601pwd.DeviceUtils

class LoginDataSource {

    companion object {
        private const val SP_CODE = "sp_code"
    }

    fun getSerialNumber(context: Context): String {
        return DeviceUtils.getDeviceId(context)
    }

    fun login(serialNumber: String?, code: String?): Boolean {
        if (serialNumber.isNullOrEmpty()) {
            throw IllegalArgumentException("序列号不能为空")
        }
        if (code.isNullOrEmpty()) {
            throw IllegalArgumentException("激活码不能为空")
        }
        return DeviceUtils.calPassword(serialNumber) == code
    }

    fun getCode(): String {
        return SPUtils.getInstance().get(SP_CODE, "")
    }

    fun saveCode(code: String?) {
        SPUtils.getInstance().put(SP_CODE, code)
    }

}