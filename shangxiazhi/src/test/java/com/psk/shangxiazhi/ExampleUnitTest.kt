package com.psk.shangxiazhi

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        val interval = 600L
        val sendOrderInterval = 300L
        val cost = measureTimeMillis {
            if (interval < sendOrderInterval) {
                delay(interval)
            } else {
                var remain = interval
                while (remain > 0) {
                    val d = remain.coerceAtMost(sendOrderInterval)
                    delay(d)
                    remain -= sendOrderInterval
                    if (d >= sendOrderInterval) {
                        println("握手")
                    }
                }
            }
        }
        println(cost)
    }

}