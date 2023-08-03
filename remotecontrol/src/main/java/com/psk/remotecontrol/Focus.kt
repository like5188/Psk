package com.psk.remotecontrol

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

/*
1、所有 View 都可用的状态有 selected 和 activated，这两个状态的设置有个共同的特点，就是会递归设置子 View 的状态。
其他状态例如 pressed 是系统使用的我们最好不要直接调用 setPressed()，
checked 状态是实现了 Checkable 接口的 View 才有的，例如 CheckBox、RadioButton 等。

2、焦点作为 View 的一种状态，与其他状态不同的是，它是一个“全局”的状态，
在一个 Window 上同时只有一个 View 能变成 focused 状态，也就是说在屏幕上只有一个 View 能获焦。
 */
/**
 * 设置 "焦点聚合" 监听
 *
 * 当 ViewGroup 内部任意一个子孙 View 获得焦点时，就认为 ViewGroup "聚焦"了，否则 "失焦"。这里称之为 "焦点聚合"，together focus
 */
fun ViewGroup.setOnTogetherFocusChangeListener(listener: View.OnFocusChangeListener) {
    var lastFocused = false
    val allChildren = getAllChildren()
    allChildren.forEach {
        it.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
            // 这里不能直接用 ViewGroup.hasFocused() 方法。因为它有时候不能返回正确的结果。https://www.jianshu.com/p/bde5ddbed613
            val curFocused = allChildren.any { it.isFocused }
            if (lastFocused != curFocused) {
                listener.onFocusChange(this, curFocused)
                lastFocused = curFocused
            }
        }
    }
}

private fun ViewGroup.getAllChildren(): List<View> {
    val result = mutableListOf<View>(this)
    children.forEach {
        if (it is ViewGroup) {
            result.addAll(it.getAllChildren())
        } else {
            result.add(it)
        }
    }
    return result
}
