package com.psk.shangxiazhi.train

import android.content.Context
import android.text.Selection
import android.util.AttributeSet
import android.view.FocusFinder
import android.view.KeyEvent
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText

/**
 * 由于 TextView 中把方向键的监听屏蔽了。不再向下分发，所以我们监听不到方向键。
 * 这里我们自己处理方向上、下键。
 */
class FocusEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(context, attrs) {

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val selectionStart: Int = Selection.getSelectionStart(text)
            // 当前光标所在行
            val currentCursorLine = if (selectionStart == -1) -1 else layout.getLineForOffset(selectionStart)
            // 计算光标应该移动的方向
            var direction = 0
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> if (currentCursorLine == 0) {
                    direction = FOCUS_UP
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> if (currentCursorLine + 1 == lineCount) {
                    direction = FOCUS_DOWN
                }
            }
            if (direction > 0) {
                if (rootView != null && rootView is ViewGroup) {
                    // 查找下一个应该获得焦点的控件
                    val nextFocusView = FocusFinder.getInstance().findNextFocus(rootView as ViewGroup, this, direction)
                    if (nextFocusView != null) {
                        nextFocusView.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}