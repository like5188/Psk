package com.psk.shangxiazhi.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.like.common.util.SPUtils

/*
{
    "msg":"success",
    "code":0,
    "login":{
        "isLogin":1,
        "patient_token":"3efea910-66ee-4a01-9756-c91909c979b3",
        "type":1
    }
}
 */
data class Login(
    val isLogin: Int,
    val doctor_token: String,
    val patient_token: String,
    val type: Int
) {
    companion object {
        private const val SP_LOGIN = "sp_login"

        fun setCache(login: Login?) {
            val jsonString = if (login == null) {
                null
            } else {
                try {
                    Gson().toJson(login)
                } catch (e: Exception) {
                    null
                }
            }
            SPUtils.getInstance().put(SP_LOGIN, jsonString)
        }

        fun getCache(): Login? {
            val loginJsonString = SPUtils.getInstance().get<String?>(SP_LOGIN, null)
            if (loginJsonString.isNullOrEmpty()) {
                return null
            }
            return try {
                Gson().fromJson<Login?>(loginJsonString, object : TypeToken<Login>() {}.type)
            } catch (e: Exception) {
                null
            }
        }
    }
}
