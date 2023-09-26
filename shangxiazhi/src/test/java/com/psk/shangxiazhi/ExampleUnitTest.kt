package com.psk.shangxiazhi

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        val list = listOf(
            A(0, 1),
            A(1, 1),
            A(3, 1),
            A(4, 0),
            A(5, 0),
            A(5, 0),
            A(6, 1),
            A(8, 0),
            A(9, 1),
        )
        var preMode = -1
        var preModeStartSeconds = 0
        var activeDuration = 0// 主动时长
        var passiveDuration = 0// 被动时长
        list.asFlow().collectLatest {
            if (preMode != it.mode) {
                when (it.mode) {
                    0 -> {// 如果当前是主动，那么以前就是被动
                        passiveDuration += it.seconds - preModeStartSeconds
                    }

                    1 -> {// 如果当前是被动，那么以前就是主动
                        activeDuration += it.seconds - preModeStartSeconds
                    }
                }
                println("activeDuration=$activeDuration passiveDuration=$passiveDuration")
                preMode = it.mode
                preModeStartSeconds = it.seconds
            }
        }
    }

    data class A(val seconds: Int, val mode: Int)

}