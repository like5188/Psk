package com.psk.common.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import java.text.DecimalFormat

class DefaultDigitalClock(private val maxSeconds: Long = -1, private val clock: ClockOnMainThread) {
    private var mTicker: Ticker? = null
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == TICK_EVENT) {
            val seconds = msg.obj as Long
            val HHMMss = formatElapsedTime(seconds)
            clock.tick(seconds, HHMMss)
        }
        false
    }
    private var startTime: Long = -1
    private var elapsedSeconds: Long = -1
    private val decimalFormat = DecimalFormat("00")

    fun start() {
        startTime = System.currentTimeMillis()
        mTicker = Ticker()
        val now = SystemClock.uptimeMillis()
        val next = now + (1000 - now % 1000)// 整秒。减去消耗的时间。
        handler.postAtTime(mTicker!!, next)
    }

    fun stop() {
        handler.removeMessages(TICK_EVENT)
        if (mTicker != null) {
            handler.removeCallbacks(mTicker!!)
        }
    }

    fun reset() {
        elapsedSeconds = -1
        startTime = -1
        handler.sendMessage(newTick(0))
    }

    fun restart() {
        stop()
        reset()
        start()
    }

    /**
     * 在每秒的整点执行
     * []//blog.csdn.net/cpcpcp123/article/details/88542113"">&quot;https://blog.csdn.net/cpcpcp123/article/details/88542113&quot;
     */
    private inner class Ticker : Runnable {
        override fun run() {
            onTimeChanged()
            // 在设定秒数后结束
            if (maxSeconds > 0 && elapsedSeconds == maxSeconds) {
                stop()
                return
            }
            val now = SystemClock.uptimeMillis()
            val next = now + (1000 - now % 1000)
            handler.postAtTime(this, next)
        }
    }

    /**
     * 计算时间变化
     */
    private fun onTimeChanged() {
        if (Thread.currentThread() !== Looper.getMainLooper().thread) {
            Log.e(TAG, "onTimeChanged() must work on main thread!")
            return
        }
        elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        Log.d(TAG, elapsedSeconds.toString())
        val HHMMss = formatElapsedTime(elapsedSeconds)
        clock.tick(elapsedSeconds, HHMMss)
    }

    private fun newTick(seconds: Long): Message {
        val msg = Message()
        msg.what = TICK_EVENT
        msg.obj = seconds
        return msg
    }

    /**
     * @see android.text.format.DateUtils.formatElapsedTime
     * @param elapsedSeconds 经过的秒数
     */
    private fun formatElapsedTime(elapsedSeconds: Long): String {
        // Break the elapsed seconds into hours, minutes, and seconds.
        var elapsedSeconds = elapsedSeconds
        var hours: Long = 0
        var minutes: Long = 0
        var seconds: Long = 0
        if (elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600
            elapsedSeconds -= hours * 3600
        }
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60
            elapsedSeconds -= minutes * 60
        }
        seconds = elapsedSeconds
        val hh = decimalFormat.format(hours)
        val mm = decimalFormat.format(minutes)
        val ss = decimalFormat.format(seconds)
        return String.format("%s:%s:%s", hh, mm, ss)
    }

    interface ClockOnMainThread {
        fun tick(seconds: Long, time: String?)
    }

    companion object {
        private const val TAG = "DefaultDigitalClock"
        private const val TICK_EVENT = 0x1001
    }
}