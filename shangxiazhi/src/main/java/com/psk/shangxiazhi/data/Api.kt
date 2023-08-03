package com.psk.shangxiazhi.data

import com.google.gson.JsonObject
import com.psk.shangxiazhi.data.model.LoginResult
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit 需要的接口
 */
interface Api {

    /**
     * 登录
     */
    @POST("pad/login")
    suspend fun login(@Body params: JsonObject?): LoginResult?

}