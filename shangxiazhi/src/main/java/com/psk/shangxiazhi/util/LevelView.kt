package com.psk.shangxiazhi.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.like.common.util.dp
import com.psk.shangxiazhi.R

class LevelView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var min: Int = 0
    private var max: Int = 0
    private var step: Int = 0
    private var cur: Int = 0

    init {
        orientation = HORIZONTAL
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
        val count = (max - min) / step + 1
        repeat(count) { curIndex ->
            val view = View(context)
            view.setBackgroundResource(R.drawable.background_control_selector_commend_color)
            view.setOnClickListener {
                children.forEachIndexed { index, view ->
                    view.isSelected = index <= curIndex
                }
            }
            val lp = LayoutParams(10.dp, 40.dp)
            if (curIndex > 0) {
                lp.marginStart = 10.dp
            }
            addView(view, lp)
        }
    }

}