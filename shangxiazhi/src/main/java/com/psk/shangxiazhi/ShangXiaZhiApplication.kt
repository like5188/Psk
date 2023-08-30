package com.psk.shangxiazhi

import com.like.common.util.ApplicationHolder
import com.like.common.util.SPUtils
import com.like.common.util.SerializableUtils
import com.psk.ble.bleModule
import com.psk.common.CommonApplication
import com.psk.device.util.deviceModule
import com.psk.shangxiazhi.util.shangXiaZhiModule
import org.koin.core.context.loadKoinModules

class ShangXiaZhiApplication : CommonApplication() {
    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.onCreate(this)
        loadKoinModules(shangXiaZhiModule)
        loadKoinModules(deviceModule)
        loadKoinModules(bleModule)

        SPUtils.getInstance().init(this)
        SerializableUtils.getInstance().init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ApplicationHolder.onTerminate()
    }
}
