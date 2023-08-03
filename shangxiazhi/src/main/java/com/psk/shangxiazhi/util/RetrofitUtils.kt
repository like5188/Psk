package com.psk.shangxiazhi.util

import com.like.retrofit.RequestConfig
import com.like.retrofit.common.CommonRetrofit
import retrofit2.Converter

/**
 * 对 Retrofit 进行封装的工具类。可以使用此工具类进行网络请求、上传、下载等操作。
 *
 * @author like
 * Date: 2021-01-08
 */
class RetrofitUtils private constructor() {
    companion object {
        fun getInstance() = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = RetrofitUtils()
    }

    // 用于普通网络接口请求的工具
    val mCommonRetrofit: CommonRetrofit by lazy { CommonRetrofit() }

    internal fun init(requestConfig: RequestConfig, gsonConverterFactory: Converter.Factory) {
        mCommonRetrofit.init(requestConfig, gsonConverterFactory)
    }

    /**
     * 获取自定义的api服务类实例
     * @return T 的实例
     */
    inline fun <reified T> getService(): T = mCommonRetrofit.getService()

}