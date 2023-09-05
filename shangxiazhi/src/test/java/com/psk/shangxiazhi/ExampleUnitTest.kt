package com.psk.shangxiazhi

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
        val bloodOxygenList = listOf(
            AAA(5, 100),
            AAA(3, 100),
            AAA(114, 101),
        )
        val bloodPressureList = listOf(
            AAA(7, 100),
            AAA(1, 100),
            AAA(112, 101),
            AAA(113, 101),
        )
        val heartRateList = listOf(
            AAA(8, 100),
            AAA(111, 101),
        )
        val shangXiaZhiList = listOf(
            AAA(2, 100),
            AAA(4, 100),
            AAA(6, 100),
            AAA(115, 101),
            AAA(110, 101),
        )
        // 1,110
        println(getDataTimeLines(bloodOxygenList, bloodPressureList, heartRateList, shangXiaZhiList))
    }

    data class AAA(
        val time: Long = System.currentTimeMillis() / 1000,
        val medicalOrderId: Long
    )

    private fun getDataTimeLines(
        bloodOxygenList: List<AAA>?,
        bloodPressureList: List<AAA>?,
        heartRateList: List<AAA>?,
        shangXiaZhiList: List<AAA>?
    ): List<Long> {
        val bloodOxygenTimeLines = mutableMapOf<Long, AAA>()
        val bloodPressureTimeLines = mutableMapOf<Long, AAA>()
        val heartRateTimeLines = mutableMapOf<Long, AAA>()
        val shangXiaZhiTimeLines = mutableMapOf<Long, AAA>()
        bloodOxygenList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            bloodOxygenTimeLines[it.key] = it.value.minBy { it.time }
        }

        bloodPressureList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            bloodPressureTimeLines[it.key] = it.value.minBy { it.time }
        }

        heartRateList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            heartRateTimeLines[it.key] = it.value.minBy { it.time }
        }

        shangXiaZhiList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            shangXiaZhiTimeLines[it.key] = it.value.minBy { it.time }
        }
        val timeLines = mutableMapOf<Long, Long>()
        bloodOxygenTimeLines.forEach {
            timeLines[it.key] = it.value.time
        }
        bloodPressureTimeLines.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.time)
        }
        heartRateTimeLines.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.time)
        }
        shangXiaZhiTimeLines.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.time)
        }
        return timeLines.values.toList()
    }
}