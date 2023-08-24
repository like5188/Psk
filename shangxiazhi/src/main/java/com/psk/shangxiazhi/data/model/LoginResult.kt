package com.psk.shangxiazhi.data.model

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
data class LoginResult(
    val code: Int,
    val msg: String,
    val login: Login
)

data class Login(
    val isLogin: Int,
    val doctor_token: String,
    val patient_token: String,
    val type: Int
)
