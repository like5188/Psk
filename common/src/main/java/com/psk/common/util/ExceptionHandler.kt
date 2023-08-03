package com.psk.common.util

import android.content.Context

/**
 * 统一错误处理工具类
 * @author like
 * Date: 2021-01-21
 */
object ExceptionHandler {

    /**
     * 处理错误
     */
    fun handle(context: Context, throwable: Throwable) {
        // 取消协程的操作需要忽略
        if (throwable is kotlinx.coroutines.CancellationException) {
            return
        }
    }

}