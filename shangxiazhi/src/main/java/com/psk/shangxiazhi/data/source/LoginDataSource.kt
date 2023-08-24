package com.psk.shangxiazhi.data.source

import android.os.Build
import com.psk.shangxiazhi.data.Api
import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.util.RetrofitUtils
import com.psk.shangxiazhi.util.toJsonObjectForApi

class LoginDataSource {

    /**
     * @param type  用户类型。患者：1；医生：2；
     */
    suspend fun load(phone: String?, password: String?, type: Int?): LoginResult? {
        if (phone.isNullOrEmpty()) {
            throw IllegalArgumentException("mobile is null or empty")
        }
        if (password.isNullOrEmpty()) {
            throw IllegalArgumentException("password is null or empty")
        }
        if (type == null) {
            throw IllegalArgumentException("type is null")
        }
        return RetrofitUtils.getInstance().getService<Api>()
            .login(
                mapOf(
                    "account" to phone,
                    "password" to password,
                    "type" to type,
                    "loginType" to 1,
                    "device" to "TV",
                    "appName" to Build.DEVICE
                ).toJsonObjectForApi()
            )
    }

}