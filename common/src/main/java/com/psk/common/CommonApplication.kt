package com.psk.common

import android.app.Application
import com.psk.common.util.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

open class CommonApplication : Application() {
    companion object {
        lateinit var sInstance: Application
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        //初始化koin
        startKoin {
            androidContext(this@CommonApplication)
        }
        loadKoinModules(commonModule)
    }

}
