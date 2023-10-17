package com.psk.common.customview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.like.common.util.SecondsTimer
import com.psk.common.R
import com.psk.common.databinding.CommonDialogCountDownTimerProgressBinding

/**
 * 带倒计时的进度条对话框
 *
 * @param countDownTime     倒计时长。秒
 * @param onCanceled        取消按钮被点击时回调
 * @param onFinished        倒计时结束时回调
 */
class CountDownTimerProgressDialog(
    context: Context,
    private val text: String = "",
    private val countDownTime: Long = 60,
    private val onCanceled: (() -> Unit)? = null,
    private val onFinished: (() -> Unit)? = null,
) : Dialog(context) {
    private val mBinding: CommonDialogCountDownTimerProgressBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_dialog_count_down_timer_progress, null, false)
    }

    // 计时器
    private val secondsTimer by lazy {
        SecondsTimer().apply {
            onTick = {
                val secondsUntilFinished = countDownTime - it
                mBinding.tvTicker.text = secondsUntilFinished.toString()
                if (secondsUntilFinished == 0L) {
                    dismiss()
                    onFinished?.invoke()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //如果需要dialog显示在软键盘之上，就需要为window添加FLAG_ALT_FOCUSABLE_IM这个属性
        window?.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        // 设置背景透明，并去掉 dialog 默认的 padding
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (countDownTime > 0L) {
            secondsTimer.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setContentView(mBinding.root)
        mBinding.tvContent.text = this@CountDownTimerProgressDialog.text
        mBinding.btnCancel.setOnClickListener {
            dismiss()
            onCanceled?.invoke()
        }
    }

    override fun dismiss() {
        secondsTimer.stop()
        mBinding.tvTicker.text = ""
        super.dismiss()
    }
}