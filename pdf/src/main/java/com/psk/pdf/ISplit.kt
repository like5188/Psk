package com.psk.pdf

import android.view.View

/**
 * View 分割者接口
 */
interface ISplit {
    /**
     * 获取用于分页的分割线集合。
     * 此分割线并不是一定会分页，而是在页面填满后再进行分页，只是作为分页的依据。
     * 如果返回空，则不会分页，即是将整个页面保存为一页。
     *
     * @param view      要分页的视图
     */
    fun getSplitLines(view: View): List<Int>
}