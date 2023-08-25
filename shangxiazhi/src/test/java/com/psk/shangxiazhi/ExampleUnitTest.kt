package com.psk.shangxiazhi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        val job = launch {
            testDelay(this).collectLatest {
                println(it)
            }
        }
        delay(6000)
        job.cancel()
        println("44444444444")
    }

    /**
     * 测试 delay 实际延迟。
     * 由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
     */
    private fun testDelay(scope: CoroutineScope): Flow<Int> {
        println("1111111111")
        scope.launch {
            flowOf(10, 11, 12).onEach { delay(1000) }.collectLatest {
                println(it)
            }
        }
        println("3333333333333")
        return flowOf(0, 1, 2).onEach { delay(1000) }
    }
}