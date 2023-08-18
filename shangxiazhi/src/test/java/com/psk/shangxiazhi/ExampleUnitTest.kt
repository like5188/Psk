package com.psk.shangxiazhi

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        withTimeout(1000) {
            var startTime = 0L
            repeat(200) {
                startTime = System.currentTimeMillis()
                delay(1)
                println("耗时：${System.currentTimeMillis() - startTime} $it")
            }
        }
    }
}