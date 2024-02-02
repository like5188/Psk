package com.psk.ecg

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun View.replaceEcgChartView(): View = withContext(Dispatchers.IO) {
    when (val view = this@replaceEcgChartView) {
        is EcgChartView -> view.toImageView()

        is ViewGroup -> {
            traverseViews(view) {
                if (it is EcgChartView) {
                    val parent = it.parent as ViewGroup
                    val index = parent.indexOfChild(it)
                    withContext(Dispatchers.Main) {
                        parent.removeViewAt(index)
                        parent.addView(it.toImageView(), index)
                    }
                }
            }
            view
        }

        else -> view
    }
}

private fun EcgChartView.toImageView(): ImageView = ImageView(this.context).also {
    it.layoutParams = this.layoutParams
    it.background = this.background
    it.setImageBitmap(this.getBitmap())
}

private suspend fun traverseViews(viewGroup: ViewGroup, onEach: suspend (View) -> Unit) {
    onEach(viewGroup)
    for (i in 0 until viewGroup.childCount) {
        val child = viewGroup.getChildAt(i)
        if (child is ViewGroup) {
            traverseViews(child, onEach)
        } else {
            onEach(child)
        }
    }
}