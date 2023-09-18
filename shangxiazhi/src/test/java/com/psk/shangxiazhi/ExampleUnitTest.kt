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
        val data = byteArrayOf(0x7F.toByte(), 0x7E.toByte())
        val v0: Int = data[0].toInt() and 0xff shl 8
        val v1: Int = data[1].toInt() and 0xff
        val sbp: Int = v0 + v1
        val a = 0xFF
        println(a)
        println(a.toUByte())
        println("v0=$v0 v1=$v1 sbp=$sbp")
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