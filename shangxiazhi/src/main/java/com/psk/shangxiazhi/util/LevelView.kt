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
    private var desPrefix: String = ""// 描述前缀
    private var desSuffix: String = ""// 描述后缀

    private var minNumber: Int = 0// 真实数据的最小值
    private var maxNumber: Int = 0// 真实数据的最大值
    private var numberPerLevel: Int = 0// 每个等级代表的真实数据步进
    private var curNumber = 0// 当前真实数据。（因为一个进度有可能表示多个数值）

    private val minLevel = 1
    private var maxLevel = 1
    private val curLevel = ObservableInt(0)// 当前等级，和 addLevelView() 方法添加的 levelView 一一对应

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.LevelView)
        try {
            desPrefix = a.getString(R.styleable.LevelView_desPrefix) ?: ""
            desSuffix = a.getString(R.styleable.LevelView_desSuffix) ?: ""
            minNumber = a.getInt(R.styleable.LevelView_minNumber, 0)
            maxNumber = a.getInt(R.styleable.LevelView_maxNumber, 0)
            numberPerLevel = a.getInt(R.styleable.LevelView_numberPerLevel, 0)
            curNumber = a.getInt(R.styleable.LevelView_curNumber, 0)
            maxLevel = a.getInt(R.styleable.LevelView_maxLevel, 0)
        } finally {
            a.recycle()
        }
        if (minNumber <= 0) {
            throw IllegalArgumentException("LevelView minNumber is invalid")
        }
        if (maxNumber <= 0) {
            throw IllegalArgumentException("LevelView maxNumber is invalid")
        }
        if (numberPerLevel <= 0) {
            throw IllegalArgumentException("LevelView numberPerLevel is invalid")
        }
        if (curNumber < minNumber || curNumber > maxNumber) {
            throw IllegalArgumentException("LevelView curNumber is invalid")
        }
        if (maxLevel <= 0) {
            throw IllegalArgumentException("LevelView maxLevel is invalid")
        }

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
                            view.isEnabled = level < maxNumber
                        }

                        index == childCount - 2 && view is TextView -> {// 描述文本
                            view.text = "$desPrefix$level$desSuffix"
                        }

                        index == childCount - 3 -> {//减号图片
                            view.isEnabled = level > minNumber
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
            val newNumber = curNumber - numberPerLevel
            if (level <= minNumber) {
                curLevel.set(minNumber)
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
            if (level >= maxNumber) {
                curLevel.set(maxNumber)
            } else {
                curLevel.set(level)
            }
        }
        LayoutParams(30.dp, 30.dp).apply {
            addView(view, this)
        }
    }

}