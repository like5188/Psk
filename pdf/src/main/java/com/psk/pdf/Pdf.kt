package com.psk.pdf

import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.util.Size
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.IOException

/**
 * @param paperSize         纸张尺寸，mm，默认为 A4 纸尺寸
 * @param headerHeight      页眉高度，px
 * @param footerHeight      页脚高度，px
 * @param decorDrawer       页眉页脚绘制者
 * @param split             View 分割者
 */
class Pdf @JvmOverloads constructor(
    private val paperSize: Size = Size(210, 297),
    private val headerHeight: Int = 100,
    private val footerHeight: Int = 100,
    private val decorDrawer: IDecorDrawer = DefaultDecorDrawer(),
    private val split: ISplit = DefaultSplit()
) {

    /**
     * 把 View 保存为 PDF 文件
     *
     * @param view      需要保存为 PDF 的 View
     * @param file      PDF 文件
     */
    suspend fun saveView(view: View, file: File) {
        createPdfDocument(view).writeToFile(file)
    }

    /**
     * 把 pdf 文档写入文件
     */
    private suspend fun PdfDocument.writeToFile(file: File) = withContext(Dispatchers.IO) {
        file.outputStream().use {
            try {
                this@writeToFile.writeTo(it)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                this@writeToFile.close()
            }
        }
    }

    /**
     * 把 View 生成 PDF，自动根据 [paperSize] 进行分页
     */
    private suspend fun createPdfDocument(view: View): PdfDocument = withContext(Dispatchers.Main) {
        val pagers = view.splitPagers()
        val pageSize = pagers.size
        // 创建 pdf 文档
        PdfDocument().apply {
            pagers.forEach {
                val pageInfo = PdfDocument.PageInfo.Builder(it.pageWidth, it.pageHeight, it.page).create()
                startPage(pageInfo).apply {
                    // 页眉页脚
                    decorDrawer.drawDecor(canvas, it.pageWidth, it.pageHeight, it.page, pageSize, headerHeight, footerHeight)
                    // 截取内容区域
                    canvas.translate(0f, headerHeight.toFloat())
                    canvas.clipRect(Rect(0, 0, it.pageWidth, it.viewEndY - it.viewStartY))
                    // 平移使得view的绘制区域在canvas的内容区域上
                    canvas.translate(0f, -it.viewStartY.toFloat())
                    // 绘制内容
                    view.draw(canvas)
                    finishPage(this)
                }
                yield()
            }
        }
    }

    private suspend fun View.splitPagers(): List<Pager> = withContext(Dispatchers.Default) {
        val pageWidth = width
        val pageHeight = (pageWidth * paperSize.height.toFloat() / paperSize.width.toFloat()).toInt()
        val pagers = mutableListOf<Pager>()
        val splitLines = split.getSplitLines(this@splitPagers)
        if (splitLines.isEmpty()) {
            pagers.add(Pager(1, pageWidth, pageHeight, 0, pageHeight - headerHeight - footerHeight))
            return@withContext pagers
        }
        // 当前页码
        var page = 1
        // 内容开始的坐标。
        var viewStartY = 0
        splitLines.forEachIndexed { index, lineY ->
            val nextChildBottom = splitLines.getOrNull(index + 1)
            if (nextChildBottom == null) {// 到底了
                pagers.add(Pager(page, pageWidth, pageHeight, viewStartY, lineY))
                return@withContext pagers
            }
            val headerLineY = pageHeight * (page - 1) + headerHeight
            val footerLineY = pageHeight * page - footerHeight
            if (headerLineY + nextChildBottom - viewStartY > footerLineY) {// 当页已满
                pagers.add(Pager(page, pageWidth, pageHeight, viewStartY, lineY))
                viewStartY = lineY
                page++
            }
        }
        return@withContext pagers
    }

    class Pager(val page: Int, val pageWidth: Int, val pageHeight: Int, val viewStartY: Int, val viewEndY: Int)

}
