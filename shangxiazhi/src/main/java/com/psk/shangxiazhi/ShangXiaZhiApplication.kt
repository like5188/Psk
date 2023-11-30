package com.psk.shangxiazhi

import com.like.common.util.ApplicationHolder
import com.like.common.util.SPUtils
import com.like.common.util.SerializableUtils
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.data.db.ShangXiaZhiDatabaseManager
import com.psk.shangxiazhi.util.shangXiaZhiModule
import org.koin.core.context.loadKoinModules

class ShangXiaZhiApplication : CommonApplication() {
    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.onCreate(this)
        loadKoinModules(shangXiaZhiModule)

        SPUtils.getInstance().init(this)
        SerializableUtils.getInstance().init(this)
        ShangXiaZhiDatabaseManager.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ApplicationHolder.onTerminate()
    }
}
