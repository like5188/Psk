package com.psk.shangxiazhi

import com.like.common.util.ApplicationHolder
import com.like.common.util.SPUtils
import com.like.common.util.SerializableUtils
import com.like.retrofit.RequestConfig
import com.like.retrofit.interceptor.NetworkMonitorInterceptor
import com.psk.common.BuildConfig
import com.psk.common.CommonApplication
import com.psk.device.deviceModule
import com.psk.shangxiazhi.util.RetrofitUtils
import com.psk.shangxiazhi.util.shangXiaZhiModule
import org.koin.core.context.loadKoinModules
import retrofit2.converter.gson.GsonConverterFactory

class ShangXiaZhiApplication : CommonApplication() {
    companion object {
        /**
         *  测试环境
         */
        private const val BASE_URL_TEST = "http://47.92.169.14:8080/"


        /**
         *  生产环境
         */
        private const val BASE_URL_PRODUCTION = "http://prod.foohoomed.com:8080/"

        fun getBaseUrl(): String {
            return if (BuildConfig.DEBUG) BASE_URL_TEST else BASE_URL_PRODUCTION
        }
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.onCreate(this)
        loadKoinModules(shangXiaZhiModule)
        loadKoinModules(deviceModule)

        //初始化 Retrofit 工具类
        val requestConfig = RequestConfig(
            readTimeout = 60L,
            application = this,
            baseUrl = getBaseUrl(),
            interceptors = listOf(NetworkMonitorInterceptor(this))
        )
        RetrofitUtils.getInstance().init(requestConfig, GsonConverterFactory.create())

        SPUtils.getInstance().init(this)
        SerializableUtils.getInstance().init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ApplicationHolder.onTerminate()
    }
}
