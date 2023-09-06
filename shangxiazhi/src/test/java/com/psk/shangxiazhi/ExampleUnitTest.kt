package com.psk.shangxiazhi

import com.psk.shangxiazhi.history.DateAndData
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        val curTime = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.time = Date(curTime)
        println(
            DateAndData(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH),
                hour = cal.get(Calendar.HOUR),
                minute = cal.get(Calendar.MINUTE),
                second = cal.get(Calendar.SECOND),
            )
        )
        val bloodOxygenList = listOf(
            AAA(curTime + 5000, 100),
            AAA(curTime + 3000, 100),
            AAA(curTime + 114000, 101),
        )
        val bloodPressureList = listOf(
            AAA(curTime + 7000, 100),
            AAA(curTime + 1000, 100),
            AAA(curTime + 112000, 101),
            AAA(curTime + 113000, 101),
        )
        val heartRateList = listOf(
            AAA(curTime + 8000, 100),
            AAA(curTime + 111000, 101),
        )
        val shangXiaZhiList = listOf(
            AAA(curTime + 2000, 100),
            AAA(curTime + 4000, 100),
            AAA(curTime + 6000, 100),
            AAA(curTime + 115000, 101),
            AAA(curTime + 110000, 101),
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
    ): List<DateAndData> {
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

        val cal = Calendar.getInstance()
        return timeLines.map {
            cal.time = Date(it.value)
            DateAndData(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH),
                hour = cal.get(Calendar.HOUR),
                minute = cal.get(Calendar.MINUTE),
                second = cal.get(Calendar.SECOND),
                data = it.key
            )
        }
    }
}