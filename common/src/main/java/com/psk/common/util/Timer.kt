package com.psk.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform

/**
 * 延时准确的循环回调flow
 * @param delay         第一次执行时延迟多久，毫秒
 * @param period        循环执行周期，毫秒
 * @param count         循环次数。默认0。小于等于0表示不限制，无限循环
 */
fun scheduleFlow(delay: Long, period: Long, count: Int = 0): Flow<Long> =
    flow {
        val startTime = System.currentTimeMillis() + delay
        var i = 0
        while (count <= 0 || i < count) {
            emit(startTime + period * i++)
        }
    }
        .buffer()// 使用buffer操作符建立一个有64个位置的缓冲区,如果发送时发现缓冲区满了,就会挂起等待缓冲区有可用位置后再发送
        .transform {
            // 这里没有使用 delay(it - System.currentTimeMillis())，因为误差比较大。
            while (true) {
                val time = System.currentTimeMillis()
                if (time >= it) {
                    emit(time)
                    break
                }
            }
        }
        .conflate()
        .flowOn(Dispatchers.Default)
