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

    }

    /**
     * 测试 delay 实际延迟。
     * 由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
     */
    private suspend fun testDelay() {
        withTimeout(1000) {
            var startTime = 0L
            repeat(100) {
                startTime = System.currentTimeMillis()
                delay(1)
                println("耗时：${System.currentTimeMillis() - startTime} $it")
            }
        }
    }
}