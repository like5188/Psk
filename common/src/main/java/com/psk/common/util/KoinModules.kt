package com.psk.common.util

import com.google.gson.Gson
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.text.SimpleDateFormat

/**
 * File Name: KoinModules.kt
 * Description: koin 依赖注入的 module
 * Author: like
 * Date: 2023-06-12
 * Modify:
 * Date:
 */
val commonModule = module {
    // Gson
    single {
        Gson()
    }
    // SimpleDateFormat
    single(named("yyyy年MM月")) {
        SimpleDateFormat("yyyy年MM月")
    }
    single(named("yyyyMMdd")) {
        SimpleDateFormat("yyyyMMdd")
    }
    single(named("yyyy-MM-dd")) {
        SimpleDateFormat("yyyy-MM-dd")
    }
    single(named("yyyy-MM-dd HH:mm:ss")) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}
