package com.psk.app

import org.junit.Test
import kotlin.math.ceil

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val sampleRate = 250
        val interval = 1000 / sampleRate// 绘制每个数据的间隔时间
        val recommendInterval = 25// 建议循环间隔时间
        val realInterval = interval.coerceAtLeast(recommendInterval)
        val period = ceil(realInterval).toInt()// Math.ceil()向上取整
        println("realInterval=$realInterval period=$period")
        val circleTimes = 1000L / realInterval// 需要循环的次数
        val numbersOfEachDraw = ceil(sampleRate / circleTimes).toInt()
        println("circleTimes=$circleTimes numbersOfEachDraw=$numbersOfEachDraw")
    }
}