package com.psk.common.customview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.psk.common.R
import com.psk.common.databinding.CommonDialogCountDownTimerProgressBinding
import com.psk.common.util.scheduleFlow
import kotlinx.coroutines.launch

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
) : ComponentDialog(context) {
    private val mBinding: CommonDialogCountDownTimerProgressBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_dialog_count_down_timer_progress, null, false)
    }

    override fun onStart() {
        super.onStart()
        //如果需要dialog显示在软键盘之上，就需要为window添加FLAG_ALT_FOCUSABLE_IM这个属性
        window?.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        // 设置背景透明，并去掉 dialog 默认的 padding
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (countDownTime > 0L) {
            lifecycleScope.launch {
                var count = 0
                scheduleFlow(0, 1000).collect {
                    val secondsUntilFinished = countDownTime - count++
                    mBinding.tvTicker.text = secondsUntilFinished.toString()
                    println(secondsUntilFinished)
                    if (secondsUntilFinished == 0L) {
                        dismiss()
                        onFinished?.invoke()
                    }
                }
            }
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
        mBinding.tvTicker.text = ""
        super.dismiss()
    }
}