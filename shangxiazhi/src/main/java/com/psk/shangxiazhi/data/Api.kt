package com.psk.shangxiazhi.data

import com.google.gson.JsonObject
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.data.model.ResultModel
import com.psk.shangxiazhi.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit 需要的接口
 */
interface Api {

    @POST("pad/login")
    suspend fun login(@Body params: JsonObject?): ResultModel<Login?>

    @POST("pad/patient/loginOut")
    suspend fun logout(@Header("patient_token") patientToken: String): ResultModel<Any?>

    @GET("pad/patient/user/getUser")
    suspend fun getUser(@Header("patient_token") patientToken: String): ResultModel<User?>

}