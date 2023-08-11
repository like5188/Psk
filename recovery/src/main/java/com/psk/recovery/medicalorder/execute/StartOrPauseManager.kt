package com.psk.recovery.medicalorder.execute

import androidx.annotation.MainThread
import com.psk.common.util.SecondsTicker
import com.psk.device.BleManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 管理开始或者暂停"从蓝牙设备获取数据并保存到数据库中"逻辑
 */
class StartOrPauseManager(private val bleManager: BleManager) {
    private val secondsTicker: SecondsTicker by lazy {
        object : SecondsTicker(totalInterval) {
            override fun onTick(secondsUntilFinished: Long) {
                this@StartOrPauseManager.onTick?.invoke(secondsUntilFinished)
            }

            override fun onFinish() {
                onFinish?.invoke()
            }
        }
    }

    /**
     * 倒计时总时长。秒
     */
    var totalInterval: Long = 0

    /**
     * 设备断开连接触发的暂停。为了区分手动暂停。
     */
    private val isPauseByDisconnected = AtomicBoolean(false)

    /**
     * 开始执行任务
     */
    var onStart: (() -> Unit)? = null

    /**
     * 倒计时中，参数为剩余的秒数
     */
    var onTick: ((Long) -> Unit)? = null

    /**
     * 暂停后恢复执行任务
     */
    var onResume: (() -> Unit)? = null

    /**
     * 暂停执行任务
     * @param 是否为设备断开连接触发的暂停
     */
    var onPause: ((Boolean) -> Unit)? = null

    /**
     * 任务执行完毕
     */
    var onFinish: (() -> Unit)? = null

    /**
     * 成功连接一个设备时调用
     */
    @MainThread
    fun connectOne() {
        if (secondsTicker.isIdle()) {
            return
        }
        if (bleManager.isAllDeviceConnected()) {
            // 如果是设备断开连接触发的暂停，那么就自动开始。手动暂停的不开始，需要手动开始
            if (isPauseByDisconnected.get()) {
                this.onResume?.invoke()
                secondsTicker.start()
            }
        }
    }

    /**
     * 一个设备断开连接时调用
     */
    fun disconnectOne() {
        if (secondsTicker.isIdle() || secondsTicker.isPause()) {
            return
        }
        onPause(true)
    }

    /**
     * 手动开始或者暂停
     */
    @MainThread
    fun startOrPause() {
        if (secondsTicker.isRunning()) {
            onPause(false)
        } else if (bleManager.isAllDeviceConnected() && secondsTicker.isPause()) {
            this.onResume?.invoke()
            secondsTicker.start()
        } else if (bleManager.isAllDeviceConnected() && secondsTicker.isIdle()) {
            this.onStart?.invoke()
            secondsTicker.start()
        }
    }

    private fun onPause(isPauseByDisconnected: Boolean) {
        this.isPauseByDisconnected.set(isPauseByDisconnected)
        this.onPause?.invoke(isPauseByDisconnected)
        secondsTicker.pause()
    }

}
