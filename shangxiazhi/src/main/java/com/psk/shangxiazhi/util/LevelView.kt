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
import com.like.common.util.dp
import com.psk.shangxiazhi.R

class LevelView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var min: Int = 0
    private var max: Int = 0
    private var step: Int = 0
    private val curLevel: Int = 0
    private lateinit var lessView: AppCompatImageView
    private lateinit var desView: TextView
    private lateinit var addView: AppCompatImageView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.LevelView)
        try {
            min = a.getInt(R.styleable.LevelView_min, 0)
            max = a.getInt(R.styleable.LevelView_max, 0)
            step = a.getInt(R.styleable.LevelView_step, 0)
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
        addLevelView((max - min) / step + 1)
        addLessView()
        addDesView()
        addAddView()
    }

    private fun addLevelView(count: Int) {
        repeat(count) { curIndex ->
            val view = View(context)
            view.setBackgroundResource(R.drawable.background_control_selector_commend_color)
            view.setOnClickListener {
                children.forEachIndexed { index, view ->
                    view.isSelected = index <= curIndex
                }
            }
            LayoutParams(10.dp, 40.dp).apply {
                if (curIndex > 0) {
                    marginStart = 10.dp
                }
                addView(view, this)
            }
        }
    }

    private fun addLessView() {
        lessView = AppCompatImageView(context)
        lessView.setImageResource(R.drawable.less_enable)
        LayoutParams(30.dp, 30.dp).apply {
            marginStart = 30.dp
            addView(lessView, this)
        }
    }

    private fun addDesView() {
        desView = TextView(context)
        desView.setTextColor(Color.parseColor("#00C4B9"))
        desView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        desView.gravity = Gravity.CENTER
        desView.text = "3232"
        LayoutParams(80.dp, LayoutParams.WRAP_CONTENT).apply {
            marginStart = 5.dp
            marginEnd = 5.dp
            addView(desView, this)
        }
    }

    private fun addAddView() {
        addView = AppCompatImageView(context)
        addView.setImageResource(R.drawable.add_enable)
        LayoutParams(30.dp, 30.dp).apply {
            addView(addView, this)
        }
    }

}