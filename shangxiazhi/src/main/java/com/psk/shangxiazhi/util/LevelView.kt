package com.psk.shangxiazhi.util

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableInt
import com.like.common.util.dp
import com.psk.shangxiazhi.R

class LevelView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var min: Int = 0// 真实数据的最小值
    private var max: Int = 0// 真实数据的最大值
    private var step: Int = 0// 真实数据的步进
    private val curNumber = ObservableInt(0)// 当前真实数据。（因为一个进度有可能表示多个数值）
    private var desPrefix: String = ""
    private var desSuffix: String = ""

    private val minLevel = 1
    private var maxLevel = 1
    private val curLevel = ObservableInt(0)// 当前等级，和 addLevelView() 方法添加的 levelView 一一对应

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.LevelView)
        try {
            min = a.getInt(R.styleable.LevelView_min, 0)
            max = a.getInt(R.styleable.LevelView_max, 0)
            step = a.getInt(R.styleable.LevelView_step, 0)
            desPrefix = a.getString(R.styleable.LevelView_desPrefix) ?: ""
            desSuffix = a.getString(R.styleable.LevelView_desSuffix) ?: ""
        } finally {
            a.recycle()
        }
        if (min <= 0) {
            throw IllegalArgumentException("LevelView count is invalid")
        }
        if (max <= 0) {
            throw IllegalArgumentException("LevelView count is invalid")
        }
        if (step <= 0) {
            throw IllegalArgumentException("LevelView count is invalid")
        }

        maxLevel = (max - min) / step + 1
        addLevelView()
        addLessView()
        addDesView()
        addAddView()
        curLevel.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val level = (sender as ObservableInt).get()
                children.forEachIndexed { index, view ->
                    when {
                        index == childCount - 1 -> {// 加号图片
                            view.isEnabled = level < max
                        }

                        index == childCount - 2 && view is TextView -> {// 描述文本
                            view.text = "$desPrefix$level$desSuffix"
                        }

                        index == childCount - 3 -> {//减号图片
                            view.isEnabled = level > min
                        }

                        else -> {// 等级进度视图
                            view.isSelected = index <= level - 1
                        }
                    }
                }
            }
        })
        curLevel.set(minLevel)
    }

    private fun addLevelView() {
        repeat(maxLevel) {
            val view = View(context)
            view.setBackgroundResource(R.drawable.background_control_selector_commend_color)
            LayoutParams(10.dp, 40.dp).apply {
                if (it > 0) {
                    marginStart = 10.dp
                }
                addView(view, this)
            }
        }
    }

    private fun addLessView() {
        val view = AppCompatImageView(context)
        view.setImageResource(R.drawable.less_enable)
        view.setOnClickListener {
            val level = curLevel.get() - 1
            if (level <= min) {
                curLevel.set(min)
            } else {
                curLevel.set(level)
            }
        }
        LayoutParams(30.dp, 30.dp).apply {
            marginStart = 20.dp
            addView(view, this)
        }
    }

    private fun addDesView() {
        val view = TextView(context)
        view.setTextColor(Color.parseColor("#00C4B9"))
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        view.gravity = Gravity.CENTER
        LayoutParams(65.dp, LayoutParams.WRAP_CONTENT).apply {
            addView(view, this)
        }
    }

    private fun addAddView() {
        val view = AppCompatImageView(context)
        view.setImageResource(R.drawable.add_enable)
        view.setOnClickListener {
            val level = curLevel.get() + 1
            if (level >= max) {
                curLevel.set(max)
            } else {
                curLevel.set(level)
            }
        }
        LayoutParams(30.dp, 30.dp).apply {
            addView(view, this)
        }
    }

}