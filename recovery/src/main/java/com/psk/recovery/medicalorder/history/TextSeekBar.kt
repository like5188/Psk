package com.psk.recovery.medicalorder.history

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import com.psk.recovery.R
import com.psk.recovery.databinding.ViewTextSeekBarBinding
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 随进度显示文本的[android.widget.SeekBar]
 */
class TextSeekBar(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), SeekBar.OnSeekBarChangeListener {
    private val mBinding: ViewTextSeekBarBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_text_seek_bar, this, true)
    }
    private var isStart = AtomicBoolean(false)
    private val multiple = ObservableInt(1)
    var onStart: (() -> Unit)? = null
    var onPause: (() -> Unit)? = null
    var onMultipleChanged: ((Int) -> Unit)? = null
    var onProgressChangedByTouch: ((Int) -> Unit)? = null

    init {
        mBinding.seekBar.setOnSeekBarChangeListener(this)
        mBinding.ivStart.setOnClickListener {
            if (mBinding.seekBar.progress == mBinding.seekBar.max) {
                return@setOnClickListener
            }
            if (isStart.compareAndSet(false, true)) {
                onStart?.invoke()
                mBinding.ivStart.setImageResource(R.drawable.ic_pause)
            } else if (isStart.compareAndSet(true, false)) {
                onPause?.invoke()
                mBinding.ivStart.setImageResource(R.drawable.ic_start)
            }
        }
        mBinding.multiple = multiple
        mBinding.ivDown.setOnClickListener {
            val c = multiple.get()
            if (c > 1) {
                multiple.set(c - 1)
                onMultipleChanged?.invoke(multiple.get())
            }
        }
        mBinding.ivUp.setOnClickListener {
            val c = multiple.get()
            if (c < 5) {
                multiple.set(c + 1)
                onMultipleChanged?.invoke(multiple.get())
            }
        }
    }

    fun setText(text: String) {
        mBinding.tv.text = text
    }

    fun setProgress(progress: Int) {
        mBinding.seekBar.progress = progress
    }

    fun setMax(max: Int) {
        mBinding.seekBar.max = max
    }

    private fun updateTextPosition() {
        val progressPercent = mBinding.seekBar.progress.toFloat() / mBinding.seekBar.max
        val realSeekBarWidth = mBinding.seekBar.width - mBinding.seekBar.paddingStart - mBinding.seekBar.paddingEnd
        val seekBarIndicatorLeft = (mBinding.seekBar.paddingStart + realSeekBarWidth * progressPercent - mBinding.tv.width / 2f).toInt()
        val layoutParams = mBinding.tv.layoutParams as LayoutParams
        layoutParams.setMargins(seekBarIndicatorLeft, 0, 0, 0)
        mBinding.tv.layoutParams = layoutParams
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        updateTextPosition()
        if (fromUser) {
            onProgressChangedByTouch?.invoke(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (isStart.compareAndSet(true, false)) {
            onPause?.invoke()
            mBinding.ivStart.setImageResource(R.drawable.ic_start)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (isStart.compareAndSet(false, true)) {
            onStart?.invoke()
            mBinding.ivStart.setImageResource(R.drawable.ic_pause)
        }
    }

}