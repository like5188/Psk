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
 * @param delay     第一次执行时延迟多久，毫秒
 * @param period    循环执行周期，毫秒
 */
fun scheduleFlow(delay: Long, period: Long): Flow<Long> =
    flow {
        val startTime = System.currentTimeMillis() + delay
        var i = 0
        while (true) {
            emit(startTime + period * i++)
        }
    }
        .buffer()// 使用buffer操作符建立一个有64个位置的缓冲区,如果发送时发现缓冲区满了,就会挂起等待缓冲区有可用位置后再发送
        .transform {
            // 这里没有使用 delay(it - System.currentTimeMillis())，因为误差比较大。
            while (true) {
                if (System.currentTimeMillis() >= it) {
                    emit(it)
                    break
                }
            }
        }
        .conflate()
        .flowOn(Dispatchers.Default)
