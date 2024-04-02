package com.psk.ecg.util

import android.annotation.SuppressLint
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import com.psk.ecg.base.BaseEcgView

@SuppressLint("ClickableViewAccessibility")
class Gesture(private val view: BaseEcgView) {
    private val gestureDetector by lazy {
        GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                for (i in 0 until rects.size) {
                    if (rects[i].contains(e.x, e.y)) {
                        onLeadsClickListener?.invoke(i)
                        break
                    }
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onDown(e: MotionEvent): Boolean {
                // 必须返回true，否则无法检测其它事件
                return true
            }
        })
    }
    private val rects = mutableListOf<RectF>()
    private var onLeadsClickListener: ((Int) -> Unit)? = null

    fun init(leadsCount: Int, onLeadsClickListener: (Int) -> Unit) {
        if (leadsCount <= 0) {
            return
        }
        view.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }
        this.onLeadsClickListener = onLeadsClickListener
        rects.clear()
        val height = view.height
        val left = view.left
        val right = view.right
        // 一个导联的高度
        val leadsH = height.toFloat() / leadsCount
        for (i in 0 until leadsCount) {
            val top = i * leadsH
            val bottom = top + leadsH
            rects.add(RectF(left.toFloat(), top, right.toFloat(), bottom))
        }
    }

}
