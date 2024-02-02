package com.psk.pdf

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.children

/**
 * @param noSplitViewList           禁止在指定的 View 后面分页。这样这个 View 会绘制在下一页中。
 * @param noSplitViewGroupList      禁止拆分的 ViewGroup，它会被视为一个整体，不会被分页。
 */
class DefaultSplit(
    private val noSplitViewList: List<View> = emptyList(),
    private val noSplitViewGroupList: List<ViewGroup> = emptyList()
) : ISplit {

    override fun getSplitLines(view: View): List<Int> {
        if (view !is ViewGroup) {
            return emptyList()
        }
        if (view is ScrollView) {
            // 注意：ScrollView 不能设置 android:scrollbarStyle="insideInset" 或者 android:scrollbarStyle="outsideInset" 否则屏幕外的视图绘制不了，只能绘制当前屏幕的视图。
            if (view.scrollBarStyle == View.SCROLLBARS_INSIDE_INSET || view.scrollBarStyle == View.SCROLLBARS_OUTSIDE_INSET) {
                throw IllegalArgumentException("ScrollView can not set scrollbarStyle to insideInset or outsideInset")
            }
        }
        return parseViewGroup(view, 0)
    }

    /**
     * 解析内部子视图，返回分割线集合。
     */
    private fun parseViewGroup(viewGroup: ViewGroup, top: Int): List<Int> {
        if (viewGroup.visibility == View.GONE) {
            return emptyList()
        }
        if (noSplitViewGroupList.contains(viewGroup)) {
            return listOf(top + viewGroup.bottom)
        }
        val list = mutableListOf<Int>()
        for (child in viewGroup.children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (noSplitViewList.contains(child)) {
                continue
            }
            if (child !is ViewGroup) {
                list.add(top + child.bottom)
                continue
            }
            list.addAll(parseViewGroup(child, top + child.top))
        }
        return list
    }

}