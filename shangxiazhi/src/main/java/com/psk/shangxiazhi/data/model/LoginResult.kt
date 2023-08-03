package com.psk.shangxiazhi.data.model

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
