package com.psk.app

import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        runBlocking {
            scheduleFlow(0, 1000).collect {
                println("1 collect $it")
                delay(2000)
                println("2 collect $it")
            }
        }
    }
}