package com.psk.recovery

import com.psk.common.CommonApplication
import com.psk.recovery.util.recoveryModule
import org.koin.core.context.loadKoinModules

class RecoveryApplication : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        loadKoinModules(recoveryModule)
    }

}
