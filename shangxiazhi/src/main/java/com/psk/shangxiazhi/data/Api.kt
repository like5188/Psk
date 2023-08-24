package com.psk.shangxiazhi.data

import com.google.gson.JsonObject
import com.psk.shangxiazhi.data.model.LoginResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit 需要的接口
 */
interface Api {

    @POST("pad/login")
    suspend fun login(@Body params: JsonObject?): LoginResult?

    @GET("pad/patient/user/getUser")
    suspend fun getUser(@Header("patient_token") patientToken: String): String?

}