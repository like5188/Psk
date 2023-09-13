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

    private var maxNumber: Int = 0// 真实数据的最大值
    private var minNumber: Int = 0// 真实数据的最小值
    private var numberStep: Int = 0// 真实数据步进，即点一次加减号，真实数据改变的数值
    private var numberPerLevel: Int = 0// 每个等级对应多少真实数据数值（因为一个进度有可能表示多个数值）
    private val curNumber = ObservableInt(0)// 当前真实数据。

    private var maxLevel = 1// 最大等级
    private val minLevel = 1// 最小等级
    private var curLevel = 1// 当前等级

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.LevelView)
        var curNumber = 0
        try {
            desPrefix = a.getString(R.styleable.LevelView_desPrefix) ?: ""
            desSuffix = a.getString(R.styleable.LevelView_desSuffix) ?: ""
            maxNumber = a.getInt(R.styleable.LevelView_maxNumber, 0)
            minNumber = a.getInt(R.styleable.LevelView_minNumber, 0)
            numberStep = a.getInt(R.styleable.LevelView_numberStep, 0)
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
        if (numberStep <= 0) {
            throw IllegalArgumentException("LevelView numberStep is invalid")
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
        addMinusView()
        addDesView()
        addAddView()
        this.curNumber.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val number = (sender as ObservableInt).get()
                children.forEachIndexed { index, view ->
                    when {
                        index == childCount - 1 -> {// 加号图片
                        }

                        index == childCount - 2 && view is TextView -> {// 描述文本
                            view.text = "$desPrefix$number$desSuffix"
                        }

                        index == childCount - 3 -> {//减号图片
                        }

                        else -> {// 等级进度视图
                            view.isSelected = index <= curLevel - 1
                        }
                    }
                }
            }
        })
        this.curNumber.set(curNumber)
    }

    fun getLevel() = curLevel

    fun getNumber() = curNumber.get()

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

    private fun addMinusView() {
        val view = AppCompatImageView(context)
        view.setImageResource(R.drawable.less_enable)
        view.setOnClickListener {
            if (curNumber.get() <= minNumber) {
                return@setOnClickListener
            }
            var newNumber = curNumber.get() - numberStep
            newNumber = if (newNumber <= minNumber) {
                minNumber
            } else {
                newNumber
            }
            if (newNumber / numberPerLevel < curLevel) {// 需要改变等级
                val newLevel = curLevel - 1
                curLevel = if (newLevel <= minLevel) {
                    minLevel
                } else {
                    newLevel
                }
            }
            // 触发界面更新
            curNumber.set(newNumber)
        }
        LayoutParams(30.dp, 30.dp).apply {
            marginStart = 30.dp
            addView(view, this)
        }
    }

    private fun addDesView() {
        val view = TextView(context)
        view.setTextColor(Color.parseColor("#00C4B9"))
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        view.gravity = Gravity.CENTER
        LayoutParams(80.dp, LayoutParams.WRAP_CONTENT).apply {
            addView(view, this)
        }
    }

    private fun addAddView() {
        val view = AppCompatImageView(context)
        view.setImageResource(R.drawable.add_enable)
        view.setOnClickListener {
            if (curNumber.get() >= maxNumber) {
                return@setOnClickListener
            }
            var newNumber = curNumber.get() + numberStep
            newNumber = if (newNumber >= maxNumber) {
                maxNumber
            } else {
                newNumber
            }
            if (newNumber / numberPerLevel > curLevel) {// 需要改变等级
                val newLevel = curLevel + 1
                curLevel = if (newLevel >= maxLevel) {
                    maxLevel
                } else {
                    newLevel
                }
            }
            // 触发界面更新
            curNumber.set(newNumber)
        }
        LayoutParams(30.dp, 30.dp).apply {
            addView(view, this)
        }
    }

}