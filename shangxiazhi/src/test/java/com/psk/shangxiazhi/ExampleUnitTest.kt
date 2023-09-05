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
            AAA(1, 100),
            AAA(7, 100),
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
        val bloodOxygenTimeLines = bloodOxygenList?.groupBy {
            it.medicalOrderId
        }?.map {
            it.value.first().time
        } ?: emptyList()
        val bloodPressureTimeLines = bloodPressureList?.groupBy {
            it.medicalOrderId
        }?.map {
            it.value.first().time
        } ?: emptyList()
        val heartRateTimeLines = heartRateList?.groupBy {
            it.medicalOrderId
        }?.map {
            it.value.first().time
        } ?: emptyList()
        val shangXiaZhiTimeLines = shangXiaZhiList?.groupBy {
            it.medicalOrderId
        }?.map {
            it.value.first().time
        } ?: emptyList()
        val timeLines = sortedSetOf(
            *(bloodOxygenTimeLines + bloodPressureTimeLines + heartRateTimeLines + shangXiaZhiTimeLines).toTypedArray()
        )
        return timeLines.toList()
    }
}