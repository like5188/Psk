package com.psk.common.util

import android.os.CountDownTimer
import androidx.annotation.MainThread

/**
 * 秒倒计时，支持暂停、恢复。
 * @param totalInterval    倒计时总时长。秒
 */
abstract class SecondTicker(totalInterval: Long) {
    private var countDownTimer: CountDownTimer? = null

    /**
     * 当前状态：
     * 0：空闲；1：运行；2：暂停；3：完毕
     */
    private var status: Int = 0

    fun isIdle() = status == 0

    fun isRunning() = status == 1

    fun isPause() = status == 2

    fun isFinish() = status == 3

    /**
     * 剩余时长
     */
    private var remainInterval = totalInterval

    @MainThread
    fun start() {
        status = 1
        // 注意：此处SecondCountDownTimer实例化必须在主线程。否则报错：Can't create handler inside thread Thread[DefaultDispatcher-worker-4,5,main] that has not called Looper.prepare()
        countDownTimer = object : SecondCountDownTimer(remainInterval, 1) {
            override fun onSecondTick(secondsUntilFinished: Long) {
                remainInterval = secondsUntilFinished
                this@SecondTicker.onTick(secondsUntilFinished)
            }

            override fun onFinish() {
                status = 3
                countDownTimer?.cancel()
                countDownTimer = null
                this@SecondTicker.onFinish()
            }
        }.start()
    }

    fun pause() {
        status = 2
        countDownTimer?.cancel()
        countDownTimer = null
    }

    abstract fun onTick(secondsUntilFinished: Long)

    abstract fun onFinish()
}

/**
 * 秒数倒计时
 */
abstract class SecondCountDownTimer(secondsInFuture: Long, countDownInterval: Long) :
    CountDownTimer(secondsInFuture * 1000, countDownInterval * 1000) {
    final override fun onTick(millisUntilFinished: Long) {
        onSecondTick(millisUntilFinished / 1000)
    }

    abstract fun onSecondTick(secondsUntilFinished: Long)
}
