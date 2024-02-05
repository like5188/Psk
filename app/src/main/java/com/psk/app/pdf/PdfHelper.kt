package com.psk.app.pdf

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.ecg.base.BaseEcgView
import com.psk.pdf.Pdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File

class PdfHelper @JvmOverloads constructor(private val pdf: Pdf = Pdf()) {
    private var job: Job? = null

    /**
     * 把 View 保存为 PDF 文件
     *
     * @param view      需要保存为 PDF 的 View
     * @param file      PDF 文件
     */
    fun save(view: View, file: File) {
        cancel()
        job = ViewTreeLifecycleOwner.get(view)?.lifecycleScope?.launch(Dispatchers.Main) {
            // 替换所有[BaseEcgView]为[ImageView]
            when (view) {
                is BaseEcgView -> pdf.saveView(view.toImageView(), file)

                is ViewGroup -> {
                    val ecgViewCaches = mutableListOf<EcgViewCache>()
                    // 替换
                    traverseViews(view) {
                        if (it is BaseEcgView) {
                            val parent = it.parent as ViewGroup
                            val index = parent.indexOfChild(it)
                            ecgViewCaches.add(EcgViewCache(it, parent, index))
                            withContext(Dispatchers.Main) {
                                parent.removeViewAt(index)
                                parent.addView(it.toImageView(), index)
                            }
                        }
                    }
                    // 保存为 Pdf
                    pdf.saveView(view, file)
                    // 还原
                    if (ecgViewCaches.isNotEmpty()) {
                        traverseViews(view) {
                            if (it is ViewGroup) {
                                val ecgViewCache = ecgViewCaches.find { ecgViewCache -> ecgViewCache.parent == it }
                                if (ecgViewCache != null) {
                                    withContext(Dispatchers.Main) {
                                        it.removeViewAt(ecgViewCache.index)
                                        it.addView(ecgViewCache.ecgView, ecgViewCache.index)
                                    }
                                }
                            }
                        }
                        ecgViewCaches.clear()
                    }
                }

                else -> pdf.saveView(view, file)
            }
        }
    }

    private class EcgViewCache(val ecgView: BaseEcgView, val parent: ViewGroup, val index: Int)

    private fun BaseEcgView.toImageView(): ImageView = ImageView(this.context).also {
        it.layoutParams = this.layoutParams
        it.background = this.background
        it.setImageBitmap(this.getBitmap())
    }

    private suspend fun traverseViews(viewGroup: ViewGroup, onEach: suspend (View) -> Unit) {
        yield()
        onEach(viewGroup)
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                traverseViews(child, onEach)
            } else {
                onEach(child)
                yield()
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

}