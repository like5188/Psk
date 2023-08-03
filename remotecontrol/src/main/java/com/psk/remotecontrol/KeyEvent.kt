package com.psk.remotecontrol

import android.view.KeyEvent

abstract class OnKeyEventClickListener {
    open fun onClickEnter() {}
    open fun onClickBack() {}
    open fun onClickUp() {}
    open fun onClickDown() {}
    open fun onClickLeft() {}
    open fun onClickRight() {}
}

@Override
fun KeyEvent?.setOnKeyEventClickListener(l: OnKeyEventClickListener): Boolean {
    this ?: return false
    if (action != KeyEvent.ACTION_UP) return false
    return when (keyCode) {
        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
            //确定键
            l.onClickEnter()
            true
        }

        KeyEvent.KEYCODE_BACK -> {
            //返回键
            l.onClickBack()
            true
        }

        KeyEvent.KEYCODE_DPAD_UP -> {
            //向上键
            l.onClickUp()
            true
        }

        KeyEvent.KEYCODE_DPAD_DOWN -> {
            //向下键
            l.onClickDown()
            true
        }

        KeyEvent.KEYCODE_DPAD_LEFT -> {
            //向左键
            l.onClickLeft()
            true
        }

        KeyEvent.KEYCODE_DPAD_RIGHT -> {
            //向右键
            l.onClickRight()
            true
        }

        else -> {
            false
        }
    }
}
