package com.psk.ble

import org.koin.dsl.module

/**
 * File Name: KoinModules.kt
 * Description: koin 依赖注入的 module
 * Author: like
 * Date: 2023-06-12
 * Modify:
 * Date:
 */
val bleModule = module {
    // BleManager
    single {
        BleManager(get())
    }

}
