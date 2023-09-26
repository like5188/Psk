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
        /*
            activeDuration=3 passiveDuration=6
         */
//        val list = listOf(
//            A(0, 1),
//            A(1, 1),
//            A(3, 1),
//            A(4, 0),
//            A(5, 0),
//            A(5, 0),
//            A(6, 1),
//            A(8, 0),
//            A(9, 1),
//        )
        /*
            activeDuration=1 passiveDuration=8
         */
        val list = listOf(
            A(0, 1),
            A(1, 1),
            A(3, 1),
            A(4, 1),
            A(5, 1),
            A(5, 1),
            A(6, 1),
            A(8, 0),
            A(9, 0),
        )
        var preMode = -1
        var preSeconds = 0
        var activeDuration = 0// 主动时长
        var passiveDuration = 0// 被动时长
        list.asFlow().collectLatest {
            when (it.mode) {
                0 -> {// 当前是主动
                    if (preMode == 1) {// 模式刚由被动变成主动
                        passiveDuration += it.seconds - preSeconds
                    } else {
                        activeDuration += it.seconds - preSeconds
                    }
                }

                1 -> {// 当前是被动
                    if (preMode == 0) {// 模式刚由主动变成被动
                        activeDuration += it.seconds - preSeconds
                    } else {
                        passiveDuration += it.seconds - preSeconds
                    }
                }
            }
            preMode = it.mode
            preSeconds = it.seconds
        }
        println("activeDuration=$activeDuration passiveDuration=$passiveDuration")
    }

    data class A(val seconds: Int, val mode: Int)

}