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
        // {100=1, 101=110}
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
    ): Map<Long, Long> {
        // 存储每次训练的 medicalOrderId 和 训练开始时间（最早的一个时间）
        val timeLines = mutableMapOf<Long, Long>()

        bloodOxygenList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.minOf { it.time })
        }

        bloodPressureList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.minOf { it.time })
        }

        heartRateList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.minOf { it.time })
        }

        shangXiaZhiList?.groupBy {
            it.medicalOrderId
        }?.forEach {
            val oldValue = timeLines.getOrDefault(it.key, Long.MAX_VALUE)
            timeLines[it.key] = Math.min(oldValue, it.value.minOf { it.time })
        }
        return timeLines
    }
}