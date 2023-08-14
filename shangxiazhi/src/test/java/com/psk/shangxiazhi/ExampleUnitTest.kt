package com.psk.shangxiazhi

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val flow = channelFlow<Int> {
        while (true) {
            delay(1000)
            trySend(111)
        }
    }
    @Test
    fun addition_isCorrect() = runBlocking{
        flow.zip((0..100).asFlow().onEach {
            delay(1000)
        }.conflate()){a,b-> println("$a $b")}
        .collect()
    }
}