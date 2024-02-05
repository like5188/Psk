package com.psk.app.pdf

import android.view.View
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.psk.pdf.Pdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        job = ViewTreeLifecycleOwner.get(view)?.lifecycleScope?.launch(Dispatchers.IO) {
            pdf.saveView(view, file)
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

}